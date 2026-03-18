#!/usr/bin/env node
// CLI entry point + Node ESM named export
// Usage: bukhari <hadithId> [-a|-b]
//        bukhari <chapterId> <hadithId> [-a|-b]
//        bukhari -h | --help
//        bukhari -v | --version

import fs   from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { Bukhari } from '../index.js';

const __filename  = fileURLToPath(import.meta.url);
const __dirname   = path.dirname(__filename);
const bukhariData = JSON.parse(
  fs.readFileSync(path.join(__dirname, 'bukhari.json'), 'utf8')
);

const bukhari = new Bukhari(bukhariData);
export default bukhari;
export { Bukhari };

// ── CLI ───────────────────────────────────────────────────────────────────────
function isRunDirectly() {
  if (!process.argv[1]) return false;
  const argv1 = path.resolve(process.argv[1]).toLowerCase().replace(/\\/g, '/');
  const self  = __filename.toLowerCase().replace(/\\/g, '/');
  const base  = path.basename(argv1).replace(/\.js$/, '');
  return argv1 === self || base === 'bukhari';
}

if (isRunDirectly()) {
  const rawArgs = process.argv.slice(2);

  // ── Flags ─────────────────────────────────────────────────────────────────
  const flags   = rawArgs.filter(a => a.startsWith('-'));
  const numArgs = rawArgs.filter(a => !a.startsWith('-'));

  const wantsHelp    = flags.some(f => f === '-h' || f === '--help');
  const wantsVersion = flags.some(f => f === '-v' || f === '--version');
  const showArabic   = flags.some(f => f === '-a' || f === '--arabic');
  const showBoth     = flags.some(f => f === '-b' || f === '--both');
  const printArabic  = showArabic || showBoth;
  const printEnglish = !showArabic || showBoth;

  // ── --version ─────────────────────────────────────────────────────────────
  if (wantsVersion) {
    const pkg = JSON.parse(
      fs.readFileSync(path.join(__dirname, '..', 'package.json'), 'utf8')
    );
    console.log('sahih-al-bukhari v' + pkg.version);
    console.log('Total hadiths : ' + bukhariData.hadiths.length);
    console.log('Total chapters: ' + bukhariData.chapters.length);
    process.exit(0);
  }

  // ── --help ────────────────────────────────────────────────────────────────
  if (wantsHelp || numArgs.length === 0) {
    console.log('');
    console.log('  Sahih al-Bukhari CLI');
    console.log('');
    console.log('  Usage:');
    console.log('    bukhari <hadithId>                   Show hadith by global ID');
    console.log('    bukhari <chapterId> <hadithId>       Show hadith within a chapter');
    console.log('');
    console.log('  Language flags:');
    console.log('    (default)                            English only');
    console.log('    -a, --arabic                         Arabic only');
    console.log('    -b, --both                           Arabic + English');
    console.log('');
    console.log('  Other flags:');
    console.log('    -h, --help                           Show this help message');
    console.log('    -v, --version                        Show version and stats');
    console.log('');
    console.log('  Examples:');
    console.log('    bukhari 1                            First hadith in English');
    console.log('    bukhari 2345 -a                      Hadith #2345 in Arabic');
    console.log('    bukhari 2345 -b                      Hadith #2345 in both languages');
    console.log('    bukhari 23 34                        34th hadith of chapter 23');
    console.log('    bukhari 23 34 --both                 Same, Arabic + English');
    console.log('');
    process.exit(0);
  }

  // ── Print hadith ──────────────────────────────────────────────────────────
  function printHadith(hadith) {
    if (!hadith) { console.error('Hadith not found.'); process.exit(1); }
    const chapter = bukhariData.chapters?.find(c => c.id === hadith.chapterId);
    const div     = '-'.repeat(60);

    console.log('\n' + div);
    if (printArabic && !printEnglish) {
      console.log(
        'حديث #' + hadith.id +
        '  |  كتاب: ' + hadith.bookId +
        '  |  باب: '  + hadith.chapterId +
        (chapter?.arabic ? ' - ' + chapter.arabic : '')
      );
    } else {
      console.log(
        'Hadith #'   + hadith.id +
        '  |  Book: '    + hadith.bookId +
        '  |  Chapter: ' + hadith.chapterId +
        (chapter?.english ? ' - ' + chapter.english : '')
      );
    }
    console.log(div);

    if (printEnglish) {
      if (hadith.english?.narrator) console.log('\n' + hadith.english.narrator);
      if (hadith.english?.text)     console.log('\n' + hadith.english.text);
    }
    if (printArabic) {
      if (printEnglish) console.log('\n' + div);
      if (hadith.arabic) console.log('\n' + hadith.arabic);
    }
    console.log('\n' + div + '\n');
  }

  // ── Resolve hadith ────────────────────────────────────────────────────────
  function resolveHadith() {
    if (numArgs.length === 1) {
      const id = parseInt(numArgs[0]);
      if (isNaN(id)) return null;
      return bukhariData.hadiths.find(h => h.id === id);
    }
    if (numArgs.length === 2) {
      const chapterId = parseInt(numArgs[0]);
      const hadithNum = parseInt(numArgs[1]);
      if (isNaN(chapterId) || isNaN(hadithNum)) return null;
      const inChapter = bukhariData.hadiths.filter(h => h.chapterId === chapterId);
      if (!inChapter.length) {
        console.error('No chapter found with id ' + chapterId + '.');
        process.exit(1);
      }
      return inChapter.find(h => h.id === hadithNum) ?? inChapter[hadithNum - 1];
    }
    return null;
  }

  const hadith = resolveHadith();
  if (!hadith) {
    console.error('Invalid arguments. Run `bukhari --help` for usage.');
    process.exit(1);
  }
  printHadith(hadith);
}