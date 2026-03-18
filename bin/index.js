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
    if (!isNaN(prop)) {
      return target[parseInt(prop)];
    }
    if (prop in target) {
      return target[prop];
    }
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
// Windows-safe: normalize paths to lowercase and unify slashes before comparing.
// npm on Windows runs the bin via a generated .cmd shim, so process.argv[1]
// will be the shim path (e.g. C:\...\bukhari), NOT this file's path.
// We therefore also accept any argv[1] whose basename is "bukhari".
function isRunDirectly() {
  if (!process.argv[1]) return false;
  const argv1 = path.resolve(process.argv[1]).toLowerCase().replace(/\\/g, '/');
  const self  = __filename.toLowerCase().replace(/\\/g, '/');
  const base  = path.basename(argv1).replace(/\.js$/, '');
  return argv1 === self || base === 'bukhari';
}

if (isRunDirectly()) {
  const args = process.argv.slice(2);

  function printHadith(hadith) {
    if (!hadith) {
      console.error('Hadith not found.');
      process.exit(1);
    }
    const chapter = bukhariData.chapters?.find(c => c.id === hadith.chapterId);
    const div = '-'.repeat(60);
    console.log('\n' + div);
    console.log('Hadith #' + hadith.id + '  |  Book: ' + hadith.bookId + '  |  Chapter: ' + hadith.chapterId + (chapter ? ' - ' + chapter.english : ''));
    console.log(div);
    if (hadith.english?.narrator) console.log('\n' + hadith.english.narrator);
    if (hadith.english?.text)     console.log('\n' + hadith.english.text);
    console.log('\n' + div + '\n');
  }

  if (args.length === 1) {
    const id = parseInt(args[0]);
    if (isNaN(id)) {
      console.error('Usage: bukhari <hadithId>  OR  bukhari <chapterId> <hadithId>');
      process.exit(1);
    }
    printHadith(hadiths.find(h => h.id === id));

  } else if (args.length === 2) {
    const chapterId = parseInt(args[0]);
    const hadithNum = parseInt(args[1]);
    if (isNaN(chapterId) || isNaN(hadithNum)) {
      console.error('Usage: bukhari <hadithId>  OR  bukhari <chapterId> <hadithId>');
      process.exit(1);
    }
    const inChapter = hadiths.filter(h => h.chapterId === chapterId);
    if (inChapter.length === 0) {
      console.error('No chapter found with id ' + chapterId + '.');
      process.exit(1);
    }
    const hadith = inChapter.find(h => h.id === hadithNum) ?? inChapter[hadithNum - 1];
    printHadith(hadith);

  } else {
    console.log('Usage:');
    console.log('  bukhari <hadithId>               - show hadith by global id');
    console.log('  bukhari <chapterId> <hadithId>   - show hadith within a chapter');
    process.exit(0);
  }
}