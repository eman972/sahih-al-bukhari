#!/usr/bin/env ruby
# frozen_string_literal: true

# Use full Ruby path for Windows
ruby_exe = 'D:\Ruby34-x64\bin\ruby.exe'
exec(ruby_exe, '-I.', __FILE__) if __FILE__ == $0

require_relative 'sahih_al_bukhari'

puts 'Testing Sahih al-Bukhari Ruby Gem...'
puts '=' * 50

begin
  # Initialize
  puts '1. Initializing collection...'
  bukhari = SahihAlBukhari::Bukhari.new
  puts "   ✓ Total hadiths: #{bukhari.length}"
  puts "   ✓ Total chapters: #{bukhari.chapters.length}"
  puts

  # Test get first hadith
  puts '2. Testing get(1)...'
  hadith = bukhari.get(1)
  puts "   ✓ Hadith #{hadith.number}"
  puts "   ✓ English: #{hadith.english[0..50]}..."
  puts

  # Test search
  puts '3. Testing search("prayer")...'
  results = bukhari.search('prayer')
  puts "   ✓ Found #{results.length} results"
  puts

  # Test random
  puts '4. Testing get_random()...'
  random = bukhari.get_random
  puts "   ✓ Random hadith: #{random.number}"
  puts

  puts 'All tests passed! ✓'

rescue => e
  puts "✗ Error: #{e.message}"
  puts "✗ Backtrace: #{e.backtrace.first(3).join(', ')}"
  exit 1
end
