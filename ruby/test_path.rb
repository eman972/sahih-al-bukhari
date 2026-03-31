#!/usr/bin/env ruby
# frozen_string_literal: true

# Test the exact same logic as the main file
THIS_DIR = File.expand_path(File.dirname(__FILE__))
REPO_ROOT = File.dirname(THIS_DIR)

data_path = File.join(REPO_ROOT, 'data', 'bukhari.json')
puts "THIS_DIR: #{THIS_DIR}"
puts "REPO_ROOT: #{REPO_ROOT}"
puts "Data path: #{data_path}"
puts "Absolute path: #{File.expand_path(data_path)}"
puts "File exists: #{File.exist?(data_path)}"

# Also test the exact condition
if File.exist?(data_path)
  puts "✓ Should find local file!"
else
  puts "✗ Will try CDN fallback"
end
