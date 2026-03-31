# frozen_string_literal: true

require_relative 'lib/sahih_al_bukhari/version'

Gem::Specification.new do |spec|
  spec.name = 'sahih-al-bukhari'
  spec.version = SahihAlBukhari::Version::VERSION
  spec.authors = ['muhammadsaadamin']
  spec.email = ['muhammadsaadamin@example.com']

  spec.summary = 'Complete Sahih al-Bukhari — 7,277 hadiths. Offline-first, zero dependencies. CLI + Ruby library.'
  spec.description = 'Complete Sahih al-Bukhari collection with 7,277 authentic hadiths featuring full Arabic text and English translations. Offline-first with zero external dependencies. Includes both Ruby library and command-line interface.'
  spec.homepage = 'https://github.com/SENODROOM/sahih-al-bukhari'
  spec.license = 'AGPL-3.0'
  spec.required_ruby_version = '>= 2.7.0'

  spec.metadata['allowed_push_host'] = 'https://rubygems.org'
  spec.metadata['homepage_uri'] = spec.homepage
  spec.metadata['source_code_uri'] = 'https://github.com/SENODROOM/sahih-al-bukhari'
  spec.metadata['changelog_uri'] = 'https://github.com/SENODROOM/sahih-al-bukhari/blob/main/CHANGELOG.md'
  spec.metadata['bug_tracker_uri'] = 'https://github.com/SENODROOM/sahih-al-bukhari/issues'

  # Specify which files should be added to the gem when it is released.
  spec.files = Dir.chdir(__dir__) do
    `git ls-files -z`.split("\x0").reject do |f|
      (f == __FILE__) || f.match(%r{\A(?:(?:bin|test|spec|features)/|\.(?:git|travis|circleci)|appveyor)})
    end
  end

  # Include the data files
  spec.files += ['data/bukhari.json.gz'] if File.exist?('data/bukhari.json.gz')

  spec.bindir = 'bin'
  spec.executables = ['bukhari']
  spec.require_paths = ['lib']

  # Runtime dependencies - none required for core functionality
  # Data is bundled in the gem, making it zero-dependency
  
  # Development dependencies (only needed for development)
  spec.add_development_dependency 'rake', '~> 13.0'
  spec.add_development_dependency 'rspec', '~> 3.0'
  spec.add_development_dependency 'rubocop', '~> 1.21'
  spec.add_development_dependency 'yard', '~> 0.9'

  # Metadata for RubyGems
  spec.metadata = {
    'rubygems_mfa_required' => 'true'
  }

  # Additional metadata
  spec.extra_rdoc_files = ['README.md', 'LICENSE']
  spec.rdoc_options = ['--main', 'README.md']

  # Test framework
  spec.test_files = Dir['spec/**/*']

  # Documentation
  spec.has_rdoc = true
end
