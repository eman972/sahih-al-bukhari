# Sahih al-Bukhari

A complete collection of Sahih al-Bukhari hadiths in JSON format with easy programmatic access for JavaScript/Node.js applications.

## Installation

```bash
npm install sahih-al-bukhari
```

## Usage

### Basic Import and Access

```javascript
import bukhari from 'sahih-al-bukhari';

// Access hadiths by index (0-based)
console.log(bukhari[0]); // First hadith
console.log(bukhari[23]); // 24th hadith
console.log(bukhari[23][0]); // If you want the first property of the 24th hadith

// Get total number of hadiths
console.log(bukhari.length); // Total hadith count
```

### Accessing by Book

```javascript
// Get all hadiths from a specific book
const book1 = bukhari.getByBook(1); // Book of Revelation
const book8 = bukhari.getByBook(8); // Book of Prayer

// Access books directly
const allBooks = bukhari.books;
const book2Hadiths = bukhari.books[2]; // All hadiths from book 2
```

### Accessing by Chapter

```javascript
// Get hadiths from a specific chapter
const chapter1 = bukhari.getByChapter(1);
```

### Searching Hadiths

```javascript
// Search in English text and narrator names
const prayerHadiths = bukhari.search('prayer');
const anasHadiths = bukhari.search('Anas');
const fastingHadiths = bukhari.search('fasting');
```

### Getting Random Hadith

```javascript
// Get a random hadith
const randomHadith = bukhari.getRandom();
console.log(randomHadith.english.text);
```

### Accessing Metadata

```javascript
// Get collection metadata
console.log(bukhari.metadata);
// Output:
// {
//   "id": 1,
//   "length": 7277,
//   "arabic": {
//     "title": "صحيح البخاري",
//     "author": "الإمام محمد بن إسماعيل البخاري",
//     "introduction": ""
//   },
//   "english": {
//     "title": "Sahih al-Bukhari",
//     "author": "Imam Muhammad ibn Ismail al-Bukhari",
//     "introduction": ""
//   }
// }

// Get chapters information
console.log(bukhari.chapters);
```

## Data Structure

Each hadith object has the following structure:

```javascript
{
  "bookId": 1,
  "chapterId": 1,
  "arabic": "حَدَّثَنَا...",
  "english": {
    "narrator": "Anas",
    "text": "The Prophet (ﷺ) said..."
  }
}
```

## Books Available

The collection includes the following books:

1. Revelation (بدء الوحى)
2. Belief (الإيمان)
3. Knowledge (العلم)
4. Ablution (الوضوء)
5. Bathing (الغسل)
6. Menstrual Periods (الحيض)
7. Rubbing hands and feet with dust (التيمم)
8. Prayer (الصلاة)
9. Times of the Prayers (مواقيت الصلاة)
10. Call to Prayers (الأذان)
... and many more

## Examples

### Example 1: Find all hadiths about prayer

```javascript
import bukhari from 'sahih-al-bukhari';

const prayerHadiths = bukhari.search('prayer');
console.log(`Found ${prayerHadiths.length} hadiths about prayer`);

prayerHadiths.slice(0, 3).forEach((hadith, index) => {
  console.log(`\n${index + 1}. ${hadith.english.narrator}:`);
  console.log(hadith.english.text);
});
```

### Example 2: Get first 10 hadiths from Book of Belief

```javascript
import bukhari from 'sahih-al-bukhari';

const beliefHadiths = bukhari.getByBook(2);
const first10 = beliefHadiths.slice(0, 10);

first10.forEach((hadith, index) => {
  console.log(`${index + 1}. ${hadith.english.narrator}:`);
  console.log(hadith.english.text.substring(0, 100) + '...');
});
```

### Example 3: Random hadith of the day

```javascript
import bukhari from 'sahih-al-bukhari';

function hadithOfTheDay() {
  const hadith = bukhari.getRandom();
  return {
    narrator: hadith.english.narrator,
    text: hadith.english.text,
    book: hadith.bookId
  };
}

console.log('Hadith of the Day:');
console.log(hadithOfTheDay());
```

## API Reference

### Properties

- `length` - Total number of hadiths
- `books` - Object with hadiths grouped by book ID
- `metadata` - Collection metadata
- `chapters` - Array of chapter information

### Methods

- `getByBook(bookId)` - Returns array of hadiths from specified book
- `getByChapter(chapterId)` - Returns array of hadiths from specified chapter
- `search(query)` - Returns array of hadiths matching the search query
- `getRandom()` - Returns a random hadith

### Array Access

- `bukhari[index]` - Access hadith by numerical index (0-based)
- `bukhari[index][property]` - Access specific property of a hadith

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Original data from Sahih al-Bukhari collection
- Translations by reputable Islamic scholars
- Formatted for programmatic access
