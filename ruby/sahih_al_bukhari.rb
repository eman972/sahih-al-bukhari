# frozen_string_literal: true

# sahih-al-bukhari — Complete Sahih al-Bukhari for Ruby.
#
# 7,277 authentic hadiths with full Arabic text and English translations.
# Shares bin/bukhari.json and chapters/ with the npm package in this repo.
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

module SahihAlBukhari
  VERSION = '3.1.2'
  CDN_BASE = 'https://cdn.jsdelivr.net/npm/sahih-al-bukhari@3.1.1/data/chapters'

  # Shared data file resolution
  # When installed from the monorepo, walk up from this file to find bin/bukhari.json
  THIS_DIR = File.expand_path(File.dirname(__FILE__))
  REPO_ROOT = File.dirname(THIS_DIR)
  INSTALLED_DATA = File.join(THIS_DIR, 'data', 'bukhari.json.gz')

  class Error < StandardError; end
  class HadithNotFoundError < Error; end
  class ChapterNotFoundError < Error; end

  # Data structure classes
  class Hadith
    attr_reader :number, :arabic, :english, :chapter_id

    def initialize(number, arabic, english, chapter_id)
      @number = number
      @arabic = arabic
      @english = english
      @chapter_id = chapter_id
    end

    def to_h
      {
        number: @number,
        arabic: @arabic,
        english: @english,
        chapter_id: @chapter_id
      }
    end

    def to_json(*args)
      to_h.to_json(*args)
    end
  end

  class Chapter
    attr_reader :id, :title, :arabic_title

    def initialize(id, title, arabic_title)
      @id = id
      @title = title
      @arabic_title = arabic_title
    end

    def to_h
      {
        id: @id,
        title: @title,
        arabic_title: @arabic_title
      }
    end

    def to_json(*args)
      to_h.to_json(*args)
    end
  end

  class Metadata
    attr_reader :total_hadiths, :total_chapters, :language, :source

    def initialize(total_hadiths, total_chapters, language, source)
      @total_hadiths = total_hadiths
      @total_chapters = total_chapters
      @language = language
      @source = source
    end

    def to_h
      {
        total_hadiths: @total_hadiths,
        total_chapters: @total_chapters,
        language: @language,
        source: @source
      }
    end

    def to_json(*args)
      to_h.to_json(*args)
    end
  end

  # Main Bukhari class
  class Bukhari
    attr_reader :metadata, :chapters, :length

    def initialize(data_path: nil)
      @data_path = data_path || find_data_file
      @data = load_data
      @metadata = Metadata.new(
        @data['metadata']['total_hadiths'],
        @data['metadata']['total_chapters'],
        @data['metadata']['language'],
        @data['metadata']['source']
      )
      @chapters = load_chapters
      @length = @metadata.total_hadiths || 0
    end

    # Get hadith by number (1-based index)
    def get(number)
      validate_hadith_number(number)
      hadith_data = @data['hadiths'][number - 1]
      Hadith.new(
        hadith_data['number'],
        hadith_data['arabic'],
        hadith_data['english'],
        hadith_data['chapter_id']
      )
    end

    # Get hadiths by chapter
    def get_by_chapter(chapter_id)
      validate_chapter_id(chapter_id)
      @data['hadiths']
        .select { |h| h['chapter_id'] == chapter_id }
        .map do |h|
          Hadith.new(h['number'], h['arabic'], h['english'], h['chapter_id'])
        end
    end

    # Search hadiths
    def search(query)
      query_lower = query.downcase
      @data['hadiths']
        .select do |h|
          h['arabic'].downcase.include?(query_lower) ||
          h['english'].downcase.include?(query_lower)
        end
        .map do |h|
          Hadith.new(h['number'], h['arabic'], h['english'], h['chapter_id'])
        end
    end

    # Get random hadith
    def get_random
      random_index = rand(@length)
      get(random_index + 1)
    end

    # Array-like access
    def [](index)
      get(index + 1) if index.is_a?(Integer)
    end

    # Iterator support
    def each(&block)
      @data['hadiths'].each_with_index do |h, index|
        hadith = Hadith.new(h['number'], h['arabic'], h['english'], h['chapter_id'])
        block.call(hadith)
      end
    end

    def to_a
      @data['hadiths'].map do |h|
        Hadith.new(h['number'], h['arabic'], h['english'], h['chapter_id'])
      end
    end

    private

    def find_data_file
      # Priority order:
      # 1. Explicit data_path parameter
      # 2. data/bukhari.json at repo root
      # 3. bin/bukhari.json at repo root
      # 4. data/bukhari.json.gz in installed package
      # 5. CDN fallback

      if File.exist?(File.join(REPO_ROOT, 'data', 'bukhari.json'))
        File.join(REPO_ROOT, 'data', 'bukhari.json')
      elsif File.exist?(File.join(REPO_ROOT, 'bin', 'bukhari.json'))
        File.join(REPO_ROOT, 'bin', 'bukhari.json')
      elsif File.exist?(INSTALLED_DATA)
        INSTALLED_DATA
      else
        download_from_cdn
      end
    end

    def load_data
      if @data_path.end_with?('.gz')
        Zlib::GzipReader.open(@data_path) do |gz|
          JSON.parse(gz.read)
        end
      else
        JSON.parse(File.read(@data_path))
      end
    rescue => e
      raise Error, "Failed to load data from #{@data_path}: #{e.message}"
    end

    def load_chapters
      chapters_data = @data['chapters'] || load_chapters_from_files
      chapters_data.map do |c|
        Chapter.new(c['id'], c['title'], c['arabic_title'])
      end
    end

    def load_chapters_from_files
      # Try to load from individual chapter files
      chapters_dir = File.join(REPO_ROOT, 'chapters')
      if Dir.exist?(chapters_dir)
        meta_file = File.join(chapters_dir, 'meta.json')
        if File.exist?(meta_file)
          meta = JSON.parse(File.read(meta_file))
          return meta['chapters']
        end
      end
      []
    end

    def download_from_cdn
      # Download main data file
      uri = URI("#{CDN_BASE}/../bukhari.json.gz")
      temp_file = File.join('C:\\temp', 'bukhari.json.gz')
      
      Net::HTTP.start(uri.host, uri.port, use_ssl: true) do |http|
        request = Net::HTTP::Get.new(uri)
        http.request(request) do |response|
          File.open(temp_file, 'wb') do |file|
            response.read_body { |chunk| file.write(chunk) }
          end
        end
      end
      
      temp_file
    rescue => e
      raise Error, "Failed to download data from CDN: #{e.message}"
    end

    def validate_hadith_number(number)
      raise HadithNotFoundError, "Hadith #{number} not found" unless @length && number.between?(1, @length)
    end

    def validate_chapter_id(chapter_id)
      raise ChapterNotFoundError, "Chapter #{chapter_id} not found" unless @chapters.any? { |c| c.id == chapter_id }
    end
  end

  # Clear any cached data
  def self.clear_cache
    # Ruby implementation doesn't cache by default, but method included for API compatibility
  end
end
