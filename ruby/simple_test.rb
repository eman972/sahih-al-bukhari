#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative 'sahih_al_bukhari'

puts 'Testing basic functionality...'
begin
  bukhari = SahihAlBukhari::Bukhari.new
  puts "✓ Success! Total hadiths: #{bukhari.length}"
rescue => e
  puts "✗ Error: #{e.message}"
  puts "✗ Backtrace: #{e.backtrace.first}"
end
