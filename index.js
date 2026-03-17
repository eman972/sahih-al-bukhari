import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load the Bukhari data
const bukhariData = JSON.parse(fs.readFileSync(path.join(__dirname, 'bukhari.json'), 'utf8'));

// Extract hadiths array for easy access
const hadiths = bukhariData.hadiths;

// Group hadiths by book for structured access
const books = {};
hadiths.forEach(hadith => {
  const bookId = hadith.bookId;
  if (!books[bookId]) {
    books[bookId] = [];
  }
  books[bookId].push(hadith);
});

// Create the main export object that allows array-like access
const bukhari = new Proxy(hadiths, {
  get(target, prop) {
    // Handle numeric indices for direct access like bukhari[23]
    if (!isNaN(prop)) {
      return target[parseInt(prop)];
    }
    
    // Handle property access
    if (prop in target) {
      return target[prop];
    }
    
    // Handle special methods
    switch (prop) {
      case 'books':
        return books;
      case 'metadata':
        return bukhariData.metadata;
      case 'chapters':
        return bukhariData.chapters;
      case 'getByBook':
        return (bookId) => books[bookId] || [];
      case 'getByChapter':
        return (chapterId) => hadiths.filter(h => h.chapterId === chapterId);
      case 'search':
        return (query) => hadiths.filter(h => 
          h.english?.text?.toLowerCase().includes(query.toLowerCase()) ||
          h.english?.narrator?.toLowerCase().includes(query.toLowerCase())
        );
      case 'getRandom':
        return () => hadiths[Math.floor(Math.random() * hadiths.length)];
      case 'length':
        return target.length;
      default:
        return target[prop];
    }
  },
  
  ownKeys(target) {
    return ['length', ...Array.from({length: target.length}, (_, i) => i.toString()), 'books', 'metadata', 'chapters', 'getByBook', 'getByChapter', 'search', 'getRandom'];
  }
});

export default bukhari;
