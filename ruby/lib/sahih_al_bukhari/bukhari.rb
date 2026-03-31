# frozen_string_literal: true

# sahih-al-bukhari — Complete Sahih al-Bukhari for Ruby.
#
# Quick start:
#   require 'sahih_al_bukhari'
#
#   bukhari = SahihAlBukhari::Bukhari.new
#   bukhari.get(1)
#   bukhari.search("prayer")
#   bukhari.get_random
#   bukhari.get_by_chapter(1)

require 'json'
require 'zlib'
require 'net/http'
require 'uri'
require 'fileutils'
require 'tmpdir'

require_relative 'version'

module SahihAlBukhari
  CDN_URL = "https://cdn.jsdelivr.net/npm/sahih-al-bukhari@#{VERSION}/data/bukhari.json.gz"

  # Locate the shared data file relative to the gem root.
  # Layout (installed gem):  lib/sahih_al_bukhari/bukhari.rb
  #                          data/bukhari.json.gz          ← same gem root
  # Layout (monorepo dev):   ruby/lib/sahih_al_bukhari/bukhari.rb
  #                          data/bukhari.json.gz          ← repo root
  _LIB_DIR  = File.expand_path('..', __dir__)        # …/ruby/lib
  _GEM_ROOT = File.expand_path('..', _LIB_DIR)       # …/ruby  (installed: gem root)
  _REPO_ROOT = File.expand_path('..', _GEM_ROOT)     # …/      (monorepo root)

  INSTALLED_DATA = File.join(_GEM_ROOT,  'data', 'bukhari.json.gz')
  REPO_DATA_GZ   = File.join(_REPO_ROOT, 'data', 'bukhari.json.gz')
  REPO_DATA_JSON = File.join(_REPO_ROOT, 'data', 'bukhari.json')

  # ---------------------------------------------------------------------------
  class Error              < StandardError; end
  class HadithNotFoundError  < Error;       end
  class ChapterNotFoundError < Error;       end

  # ---------------------------------------------------------------------------
  # Data structures
  # ---------------------------------------------------------------------------

  class Hadith
    # FIX: JSON uses keys "id" and "chapterId", NOT "number" / "chapter_id".
    # All accesses in the original code used the wrong key names, so every
    # hadith came back with nil fields.
    attr_reader :id, :number, :arabic, :english, :chapter_id

    def initialize(data)
      @id         = data['id']
      @number     = data['id']          # alias kept for backward compat
      @chapter_id = data['chapterId']
      @arabic     = data['arabic'] || ''
      _en         = data['english'] || {}
      # english field is a Hash {"narrator"=>..., "text"=>...}
      @english    = _en
      @narrator   = _en['narrator'] || ''
      @text       = _en['text']     || ''
    end

    def narrator = @narrator
    def text     = @text

    def to_h
      {
        id:         @id,
        number:     @number,
        arabic:     @arabic,
        english:    @english,
        chapter_id: @chapter_id
      }
    end

    def to_json(*args) = to_h.to_json(*args)

    def inspect
      preview = @text.length > 60 ? "#{@text[0, 60]}…" : @text
      "#<Hadith id=#{@id} chapterId=#{@chapter_id} text=#{preview.inspect}>"
    end
  end

  # ---------------------------------------------------------------------------

  class Chapter
    # FIX: JSON uses keys "english" and "arabic" for the title strings.
    # Original code passed "title" / "arabic_title" which don't exist in the data.
    attr_reader :id, :title, :arabic_title

    def initialize(data)
      @id           = data['id']
      @title        = data['english'] || ''   # FIX: key is "english", not "title"
      @arabic_title = data['arabic']  || ''   # FIX: key is "arabic", not "arabic_title"
    end

    def to_h
      { id: @id, title: @title, arabic_title: @arabic_title }
    end

    def to_json(*args) = to_h.to_json(*args)
    def inspect = "#<Chapter id=#{@id} title=#{@title.inspect}>"
  end

  # ---------------------------------------------------------------------------

  class Metadata
    # FIX: the actual JSON has a completely different metadata structure:
    #   { "id": 1, "length": 7277, "arabic": {...}, "english": {...} }
    # The original code expected flat keys like "total_hadiths", "language",
    # "source" — none of which exist — so metadata was always nil/broken.
    attr_reader :total_hadiths, :total_chapters, :language, :source,
                :arabic, :english

    def initialize(data, total_chapters)
      @total_hadiths  = data['length'] || 0
      @total_chapters = total_chapters
      @arabic         = data['arabic']  || {}
      @english        = data['english'] || {}
      @language       = 'Arabic / English'
      @source         = @english['title'] || 'Sahih al-Bukhari'
    end

    def to_h
      {
        total_hadiths:  @total_hadiths,
        total_chapters: @total_chapters,
        language:       @language,
        source:         @source,
        arabic:         @arabic,
        english:        @english
      }
    end

    def to_json(*args) = to_h.to_json(*args)
  end

  # ---------------------------------------------------------------------------
  # Main Bukhari class
  # ---------------------------------------------------------------------------

  class Bukhari
    include Enumerable

    attr_reader :metadata, :chapters, :length

    def initialize(data_path: nil)
      path  = data_path || find_data_file
      raw   = load_raw(path)

      @chapters = raw['chapters'].map { |c| Chapter.new(c) }
      @metadata = Metadata.new(raw['metadata'], @chapters.length)
      # Build Hadith objects and an O(1) lookup map
      @hadiths  = raw['hadiths'].map { |h| Hadith.new(h) }
      @by_id    = @hadiths.each_with_object({}) { |h, m| m[h.id] = h }
      @length   = @hadiths.length
    end

    # ── Public API ────────────────────────────────────────────────────────────

    # Get a hadith by its global id (1-based, matches the JSON "id" field).
    def get(id)
      @by_id[id] || raise(HadithNotFoundError, "Hadith #{id} not found")
    end

    # All hadiths belonging to chapter_id.
    def get_by_chapter(chapter_id)
      unless @chapters.any? { |c| c.id == chapter_id }
        raise ChapterNotFoundError, "Chapter #{chapter_id} not found"
      end
      @hadiths.select { |h| h.chapter_id == chapter_id }
    end

    # Full-text search across English text + narrator. Case-insensitive.
    # FIX: original called String#downcase on the `english` field which is a
    # Hash, not a String — that raised NoMethodError at runtime.
    def search(query)
      q = query.downcase
      @hadiths.select do |h|
        h.text.downcase.include?(q) || h.narrator.downcase.include?(q) ||
          h.arabic.downcase.include?(q)
      end
    end

    def get_random
      @hadiths.sample
    end

    # Array-like index access (0-based).
    def [](index)
      @hadiths[index]
    end

    # Enumerable — lets callers use .map, .select, .each, .first, etc.
    def each(&block)
      @hadiths.each(&block)
    end

    def to_a
      @hadiths.dup
    end

    # ── Private helpers ───────────────────────────────────────────────────────
    private

    def find_data_file
      candidates = [INSTALLED_DATA, REPO_DATA_GZ, REPO_DATA_JSON]
      candidates.find { |p| File.exist?(p) } || download_from_cdn
    end

    def load_raw(path)
      data = if path.end_with?('.gz')
               Zlib::GzipReader.open(path) { |gz| gz.read }
             else
               File.read(path, encoding: 'utf-8')
             end
      JSON.parse(data)
    rescue => e
      raise Error, "Failed to load data from #{path}: #{e.message}"
    end

    def download_from_cdn
      # FIX: original hard-coded 'C:\temp' — use Ruby's cross-platform tmpdir
      dest = File.join(Dir.tmpdir, 'bukhari.json.gz')
      return dest if File.exist?(dest)

      warn "[sahih-al-bukhari] Downloading data from CDN (one-time) …"
      uri = URI(CDN_URL)
      Net::HTTP.start(uri.host, uri.port, use_ssl: true) do |http|
        resp = http.get(uri.request_uri)
        raise Error, "CDN returned HTTP #{resp.code}" unless resp.is_a?(Net::HTTPSuccess)
        File.binwrite(dest, resp.body)
      end
      dest
    rescue => e
      raise Error, "Failed to download data from CDN: #{e.message}"
    end
  end

  # Clear any cached data (no-op in current implementation, kept for API compat)
  def self.clear_cache; end
end
