#!/usr/bin/env ruby
# frozen_string_literal: true

THIS_DIR = File.dirname(__FILE__)
REPO_ROOT = File.dirname(File.dirname(File.dirname(THIS_DIR)))

puts "THIS_DIR: #{THIS_DIR}"
puts "REPO_ROOT: #{REPO_ROOT}"
puts "Looking for: #{File.join(REPO_ROOT, 'data', 'bukhari.json')}"
puts "Exists: #{File.exist?(File.join(REPO_ROOT, 'data', 'bukhari.json'))}"
