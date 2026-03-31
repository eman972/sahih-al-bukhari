# frozen_string_literal: true

require 'test/unit'
require_relative '../lib/sahih_al_bukhari'

class TestSahihAlBukhari < Test::Unit::TestCase
  def setup
    @bukhari = SahihAlBukhari::Bukhari.new
  end

  def test_initialization
    assert_not_nil @bukhari
    assert @bukhari.length > 0
    assert_not_nil @bukhari.metadata
    assert_not_nil @bukhari.chapters
    assert @bukhari.chapters.length > 0
  end

  def test_get_hadith
    hadith = @bukhari.get(1)
    assert_not_nil hadith
    assert_equal 1, hadith.number
    assert_not_nil hadith.arabic
    assert_not_nil hadith.english
    assert_not_nil hadith.chapter_id
  end

  def test_get_hadith_out_of_range
    assert_raises(SahihAlBukhari::HadithNotFoundError) do
      @bukhari.get(99999)
    end
  end

  def test_search
    results = @bukhari.search('prayer')
    assert results.is_a?(Array)
    # May return empty array if no matches, but should not error
  end

  def test_random_hadith
    random = @bukhari.get_random
    assert_not_nil random
    assert random.number > 0
    assert_not_nil random.arabic
    assert_not_nil random.english
  end

  def test_get_by_chapter
    chapter_hadiths = @bukhari.get_by_chapter(1)
    assert chapter_hadiths.is_a?(Array)
    # May return empty array if chapter doesn't exist, but should not error
  end

  def test_metadata
    metadata = @bukhari.metadata
    assert_not_nil metadata.total_hadiths
    assert_not_nil metadata.total_chapters
    assert_not_nil metadata.language
    assert_not_nil metadata.source
  end

  def test_chapters
    chapters = @bukhari.chapters
    assert chapters.is_a?(Array)
    assert chapters.length > 0
    
    first_chapter = chapters.first
    assert_not_nil first_chapter.id
    assert_not_nil first_chapter.title
  end

  def test_hadith_to_h
    hadith = @bukhari.get(1)
    hash = hadith.to_h
    assert hash.is_a?(Hash)
    assert_equal hadith.number, hash[:number]
    assert_equal hadith.arabic, hash[:arabic]
    assert_equal hadith.english, hash[:english]
    assert_equal hadith.chapter_id, hash[:chapter_id]
  end

  def test_chapter_to_h
    chapter = @bukhari.chapters.first
    hash = chapter.to_h
    assert hash.is_a?(Hash)
    assert_equal chapter.id, hash[:id]
    assert_equal chapter.title, hash[:title]
    assert_equal chapter.arabic_title, hash[:arabic_title]
  end

  def test_metadata_to_h
    metadata = @bukhari.metadata
    hash = metadata.to_h
    assert hash.is_a?(Hash)
    assert_equal metadata.total_hadiths, hash[:total_hadiths]
    assert_equal metadata.total_chapters, hash[:total_chapters]
    assert_equal metadata.language, hash[:language]
    assert_equal metadata.source, hash[:source]
  end
end
