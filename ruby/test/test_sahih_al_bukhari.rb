# frozen_string_literal: true

require 'test/unit'
require_relative '../lib/sahih_al_bukhari'

class TestSahihAlBukhari < Test::Unit::TestCase
  def setup
    @bukhari = SahihAlBukhari::Bukhari.new
  end

  # ── Initialization ──────────────────────────────────────────────────────────

  def test_initialization
    assert_not_nil @bukhari
    assert @bukhari.length > 0, 'length should be positive'
    assert_not_nil @bukhari.metadata
    assert_not_nil @bukhari.chapters
    assert @bukhari.chapters.length > 0, 'should have chapters'
  end

  # ── Hadith retrieval ────────────────────────────────────────────────────────

  def test_get_hadith
    hadith = @bukhari.get(1)
    assert_not_nil hadith
    # FIX: field is .id (and .number is an alias), NOT a standalone .number key
    assert_equal 1, hadith.id
    assert_not_nil hadith.arabic,     'arabic should not be nil'
    assert_not_nil hadith.english,    'english should not be nil'
    assert_not_nil hadith.chapter_id, 'chapter_id should not be nil'
  end

  def test_get_returns_correct_hadith
    hadith = @bukhari.get(100)
    assert_equal 100, hadith.id
  end

  def test_get_hadith_out_of_range
    assert_raises(SahihAlBukhari::HadithNotFoundError) { @bukhari.get(99_999) }
  end

  def test_get_hadith_zero_raises
    assert_raises(SahihAlBukhari::HadithNotFoundError) { @bukhari.get(0) }
  end

  # ── English / Arabic fields ─────────────────────────────────────────────────

  def test_hadith_english_is_hash
    hadith = @bukhari.get(1)
    # english is a Hash with keys "narrator" and "text"
    assert_kind_of Hash, hadith.english
    assert hadith.english.key?('narrator') || hadith.english.key?('text'),
           'english hash should have narrator or text key'
  end

  def test_hadith_text_and_narrator_shortcuts
    hadith = @bukhari.get(1)
    assert_kind_of String, hadith.text
    assert_kind_of String, hadith.narrator
  end

  # ── Search ──────────────────────────────────────────────────────────────────

  def test_search_returns_array
    results = @bukhari.search('prayer')
    assert_kind_of Array, results
  end

  def test_search_results_are_hadiths
    results = @bukhari.search('prayer')
    unless results.empty?
      assert_kind_of SahihAlBukhari::Hadith, results.first
    end
  end

  def test_search_no_results_returns_empty_array
    results = @bukhari.search('xyzzy_no_match_12345')
    assert_equal [], results
  end

  # ── Random ──────────────────────────────────────────────────────────────────

  def test_random_hadith
    random = @bukhari.get_random
    assert_not_nil random
    assert random.id > 0,         'random hadith id should be positive'
    assert_not_nil random.arabic
    assert_not_nil random.english
  end

  # ── By chapter ──────────────────────────────────────────────────────────────

  def test_get_by_chapter
    chapter_hadiths = @bukhari.get_by_chapter(1)
    assert_kind_of Array, chapter_hadiths
    assert chapter_hadiths.length > 0, 'chapter 1 should have hadiths'
    chapter_hadiths.each { |h| assert_equal 1, h.chapter_id }
  end

  def test_get_by_chapter_invalid_raises
    assert_raises(SahihAlBukhari::ChapterNotFoundError) { @bukhari.get_by_chapter(99_999) }
  end

  # ── Metadata ────────────────────────────────────────────────────────────────

  def test_metadata_fields
    m = @bukhari.metadata
    assert m.total_hadiths  > 0, 'total_hadiths should be positive'
    assert m.total_chapters > 0, 'total_chapters should be positive'
    assert_kind_of String, m.language
    assert_kind_of String, m.source
  end

  def test_metadata_to_h
    h = @bukhari.metadata.to_h
    assert_kind_of Hash, h
    assert h.key?(:total_hadiths)
    assert h.key?(:total_chapters)
  end

  # ── Chapters ────────────────────────────────────────────────────────────────

  def test_chapters_array
    chapters = @bukhari.chapters
    assert_kind_of Array, chapters
    assert chapters.length > 0
  end

  def test_chapter_fields
    c = @bukhari.chapters.first
    assert_not_nil c.id
    # FIX: field is .title (mapped from JSON "english" key), not a raw .english
    assert_kind_of String, c.title,        'title should be a String'
    assert_kind_of String, c.arabic_title, 'arabic_title should be a String'
  end

  def test_chapter_to_h
    h = @bukhari.chapters.first.to_h
    assert_kind_of Hash, h
    assert h.key?(:id)
    assert h.key?(:title)
    assert h.key?(:arabic_title)
  end

  # ── to_h / to_json ──────────────────────────────────────────────────────────

  def test_hadith_to_h
    hadith = @bukhari.get(1)
    h = hadith.to_h
    assert_kind_of Hash, h
    assert_equal hadith.id,         h[:id]
    assert_equal hadith.arabic,     h[:arabic]
    assert_equal hadith.english,    h[:english]
    assert_equal hadith.chapter_id, h[:chapter_id]
  end

  def test_hadith_to_json_parses
    hadith = @bukhari.get(1)
    parsed = JSON.parse(hadith.to_json)
    assert_equal hadith.id, parsed['id']
  end

  # ── Enumerable ──────────────────────────────────────────────────────────────

  def test_enumerable_each
    count = 0
    @bukhari.each { |_h| count += 1 }
    assert_equal @bukhari.length, count
  end

  def test_array_index_access
    h = @bukhari[0]
    assert_not_nil h
    assert_kind_of SahihAlBukhari::Hadith, h
  end

  def test_to_a
    arr = @bukhari.to_a
    assert_kind_of Array, arr
    assert_equal @bukhari.length, arr.length
  end
end
