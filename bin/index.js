#!/usr/bin/env node
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

// ── CLI entry point ───────────────────────────────────────────────────────────
// Usage:
//   bukhari <hadithId>            → show hadith by its global ID
//   bukhari <chapterId> <hadithId> → show hadith #<hadithId> within chapter
// ─────────────────────────────────────────────────────────────────────────────
if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  const args = process.argv.slice(2);

  function printHadith(hadith) {
    if (!hadith) {
      console.error('Hadith not found.');
      process.exit(1);
    }
    const chapter = bukhariData.chapters?.find(c => c.id === hadith.chapterId);
    console.log('\n' + '─'.repeat(60));
    console.log(`Hadith #${hadith.id}  |  Book: ${hadith.bookId}  |  Chapter: ${hadith.chapterId}${chapter ? ` – ${chapter.english}` : ''}`);
    console.log('─'.repeat(60));
    if (hadith.english?.narrator) console.log(`\n${hadith.english.narrator}`);
    if (hadith.english?.text)     console.log(`\n${hadith.english.text}`);
    console.log('\n' + '─'.repeat(60) + '\n');
  }

  if (args.length === 1) {
    // bukhari <hadithId>  — find by global hadith id
    const id = parseInt(args[0]);
    if (isNaN(id)) { console.error('Usage: bukhari <hadithId>  OR  bukhari <chapterId> <hadithId>'); process.exit(1); }
    const hadith = hadiths.find(h => h.id === id);
    printHadith(hadith);

  } else if (args.length === 2) {
    // bukhari <chapterId> <hadithId>  — nth hadith within a chapter
    const chapterId = parseInt(args[0]);
    const hadithNum  = parseInt(args[1]);
    if (isNaN(chapterId) || isNaN(hadithNum)) {
      console.error('Usage: bukhari <hadithId>  OR  bukhari <chapterId> <hadithId>');
      process.exit(1);
    }
    const inChapter = hadiths.filter(h => h.chapterId === chapterId);
    if (inChapter.length === 0) { console.error(`No chapter found with id ${chapterId}.`); process.exit(1); }
    // Support both: exact hadith id OR positional index within chapter
    const hadith = inChapter.find(h => h.id === hadithNum) ?? inChapter[hadithNum - 1];
    printHadith(hadith);

  } else {
    console.log('Usage:');
    console.log('  node index.js <hadithId>              → hadith by global id');
    console.log('  node index.js <chapterId> <hadithId>  → hadith within a chapter');
    process.exit(0);
  }
}