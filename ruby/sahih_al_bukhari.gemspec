# frozen_string_literal: true

require_relative 'lib/sahih_al_bukhari/version'

Gem::Specification.new do |spec|
  spec.name          = 'sahih-al-bukhari'
  spec.version       = SahihAlBukhari::VERSION   # FIX: was SahihAlBukhari::Version::VERSION (wrong namespace)
  spec.authors       = ['muhammadsaadamin']
  spec.email         = ['muhammadsaadamin@example.com']

  spec.summary       = 'Complete Sahih al-Bukhari — 7,277 hadiths. Offline-first, zero dependencies. CLI + Ruby library.'
  spec.description   = 'Complete Sahih al-Bukhari collection with 7,277 authentic hadiths featuring full Arabic text and English translations. Offline-first with zero external dependencies. Includes both Ruby library and command-line interface.'
  spec.homepage      = 'https://github.com/SENODROOM/sahih-al-bukhari'
  spec.license       = 'AGPL-3.0'
  spec.required_ruby_version = '>= 2.7.0'

  # FIX: spec.metadata must be assigned ONCE — previous code assigned it twice,
  # which silently overwrote the first assignment (losing allowed_push_host etc.)
  spec.metadata = {
    'allowed_push_host'   => 'https://rubygems.org',
    'homepage_uri'        => 'https://github.com/SENODROOM/sahih-al-bukhari',
    'source_code_uri'     => 'https://github.com/SENODROOM/sahih-al-bukhari',
    'changelog_uri'       => 'https://github.com/SENODROOM/sahih-al-bukhari/blob/main/CHANGELOG.md',
    'bug_tracker_uri'     => 'https://github.com/SENODROOM/sahih-al-bukhari/issues',
    'rubygems_mfa_required' => 'true'
  }

  # FIX: `git ls-files` fails when building outside a git worktree (e.g. CI unshallow
  # clone, or after `gem build` in a temp directory).  Use Dir.glob instead so the
  # file list is always reliable.
  spec.files = Dir.chdir(__dir__) do
    Dir.glob('{bin,lib,data}/**/*', File::FNM_DOTMATCH)
       .reject { |f| File.directory?(f) }
       .concat(%w[README.md LICENSE sahih_al_bukhari.gemspec])
       .select  { |f| File.exist?(f) }
  end

  spec.bindir        = 'bin'
  spec.executables   = ['bukhari']
  spec.require_paths = ['lib']

  # Development dependencies
  spec.add_development_dependency 'rake',      '~> 13.0'
  spec.add_development_dependency 'test-unit', '~> 3.6'   # FIX: removed from Ruby 4.0 stdlib
  spec.add_development_dependency 'rspec',     '~> 3.0'
  spec.add_development_dependency 'rubocop', '~> 1.21'
  spec.add_development_dependency 'yard',    '~> 0.9'

  # FIX: spec.has_rdoc and spec.test_files are both deprecated and raise warnings
  # on modern RubyGems — removed entirely.
end
