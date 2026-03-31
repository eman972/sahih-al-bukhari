# Sahih al-Bukhari Ruby Gem

Complete Sahih al-Bukhari collection for Ruby — 7,277 authentic hadiths with full Arabic text and English translations.

## Features

- **Offline-first**: All data is bundled, no internet required
- **Zero dependencies**: Pure Ruby implementation
- **Complete collection**: All 7,277 hadiths from Sahih al-Bukhari
- **Bilingual**: Arabic text with English translations
- **CLI tool**: Command-line interface for quick access
- **Ruby library**: Easy integration into Ruby applications
- **Search functionality**: Find hadiths by text search
- **Chapter organization**: Browse by traditional chapter structure

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'sahih-al-bukhari'
```

And then execute:

```bash
$ bundle install
```

Or install it yourself as:

```bash
$ gem install sahih-al-bukhari
```

## Quick Start

### Ruby Library

```ruby
require 'sahih_al_bukhari'

# Initialize the collection
bukhari = SahihAlBukhari::Bukhari.new

# Get a specific hadith
hadith = bukhari.get(1)
puts hadith.arabic
puts hadith.english

# Search for hadiths
results = bukhari.search("prayer")
results.each { |h| puts h.english }

# Get random hadith
random = bukhari.get_random
puts random.english

# Get hadiths by chapter
chapter_hadiths = bukhari.get_by_chapter(1)

# Browse chapters
bukhari.chapters.each do |chapter|
  puts "Chapter #{chapter.id}: #{chapter.title}"
end
```

### Command Line Interface

```bash
# Get a specific hadith
bukhari -n 1

# Search for hadiths
bukhari -s prayer

# Get random hadith
bukhari -r

# List all chapters
bukhari -l

# Get hadiths from a specific chapter
bukhari -c 1

# Show metadata
bukhari -m

# Output as JSON
bukhari -n 1 --json

# Limit search results
bukhari -s prayer --limit 5
```

## API Reference

### SahihAlBukhari::Bukhari

#### Constructor

```ruby
bukhari = SahihAlBukhari::Bukhari.new(data_path: optional_custom_path)
```

#### Methods

- `get(number)` - Get hadith by number (1-based)
- `get_by_chapter(chapter_id)` - Get all hadiths from a chapter
- `search(query)` - Search hadiths by Arabic or English text
- `get_random()` - Get a random hadith
- `chapters` - Array of all chapters
- `metadata` - Collection metadata
- `length` - Total number of hadiths

#### Data Classes

**Hadith**
- `number` - Hadith number
- `arabic` - Arabic text
- `english` - English translation
- `chapter_id` - Chapter ID

**Chapter**
- `id` - Chapter ID
- `title` - English title
- `arabic_title` - Arabic title

**Metadata**
- `total_hadiths` - Total number of hadiths
- `total_chapters` - Total number of chapters
- `language` - Language codes
- `source` - Data source information

## CLI Options

```
Usage: bukhari [options]

    -n, --number NUMBER          Get specific hadith by number
    -c, --chapter CHAPTER       Get hadiths from specific chapter
    -s, --search QUERY           Search hadiths
    -r, --random                 Get random hadith
    -l, --list-chapters          List all chapters
    -m, --metadata               Show collection metadata
    -j, --json                   Output as JSON
        --chapter-info           Show chapter info with hadiths
        --limit LIMIT            Limit search results (default: 10)
    -v, --version                Show version
    -h, --help                   Show this help
```

## Data Source

This gem shares the same data files as the Node.js and Python packages in this repository:

- `bin/bukhari.json` - Main data file (shared across all packages)
- `chapters/` - Chapter metadata files

The data is automatically detected in the following priority order:

1. Custom `data_path` parameter
2. `bin/bukhari.json` at repository root
3. Bundled data in the gem
4. CDN fallback (downloads automatically)

## Development

After checking out the repo, run `bin/setup` to install dependencies. Then, run `rake spec` to run the tests.

You can also run `bin/console` for an interactive prompt that will allow you to experiment.

To install this gem onto your local machine, run `bundle exec rake install`.

### Rake Tasks

```bash
rake spec                    # Run tests
rake yard                    # Generate documentation
rake bukhari:test_basic      # Test basic functionality
rake bukhari:test_cli        # Test CLI
rake bukhari:prepare_data    # Download data files
```

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/SENODROOM/sahih-al-bukhari.

## License

This gem is available as open source under the terms of the AGPL-3.0 License.

## Version History

- **3.1.2** - Initial Ruby implementation
- Matches API compatibility with Python and Node.js versions
