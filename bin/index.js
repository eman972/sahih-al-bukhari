#!/usr/bin/env node
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { createBukhari } from '../index.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const bukhariData = JSON.parse(fs.readFileSync(path.join(__dirname, 'bukhari.json'), 'utf8'));
const bukhari = createBukhari(bukhariData);

export default bukhari;

// ── CLI entry point ───────────────────────────────────────────────────────────
function isRunDirectly() {
  if (!process.argv[1]) return false;
  const argv1 = path.resolve(process.argv[1]).toLowerCase().replace(/\\/g, '/');
  const self  = __filename.toLowerCase().replace(/\\/g, '/');
  const base  = path.basename(argv1).replace(/\.js$/, '');
  return argv1 === self || base === 'bukhari';
}

if (isRunDirectly()) {
  const rawArgs = process.argv.slice(2);
  const flags   = rawArgs.filter(a => a.startsWith('-'));
  const numArgs = rawArgs.filter(a => !a.startsWith('-'));

  const showArabic   = flags.some(f => f === '--arabic'  || f === '-a');
  const showBoth     = flags.some(f => f === '--both'    || f === '-b');
  const printArabic  = showArabic || showBoth;
  const printEnglish = !showArabic || showBoth;

  function printHadith(hadith) {
    if (!hadith) { console.error('Hadith not found.'); process.exit(1); }
    const chapter   = bukhariData.chapters?.find(c => c.id === hadith.chapterId);
    const chapterAr = chapter?.arabic || '';
    const div = '-'.repeat(60);

    console.log('\n' + div);
    if (printArabic && !printEnglish) {
      console.log('حديث #' + hadith.id + '  |  كتاب: ' + hadith.bookId + '  |  باب: ' + hadith.chapterId + (chapterAr ? ' - ' + chapterAr : ''));
    } else {
      console.log('Hadith #' + hadith.id + '  |  Book: ' + hadith.bookId + '  |  Chapter: ' + hadith.chapterId + (chapter ? ' - ' + chapter.english : ''));
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

  function resolveHadith(numArgs) {
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
      if (inChapter.length === 0) { console.error('No chapter found with id ' + chapterId + '.'); process.exit(1); }
      return inChapter.find(h => h.id === hadithNum) ?? inChapter[hadithNum - 1];
    }
    return null;
  }

  if (numArgs.length === 0) {
    console.log('Usage:');
    console.log('  bukhari <hadithId>                        - English (default)');
    console.log('  bukhari <chapterId> <hadithId>            - English (default)');
    console.log('  bukhari <hadithId> --arabic   / -a        - Arabic only');
    console.log('  bukhari <hadithId> --both     / -b        - Arabic + English');
    process.exit(0);
  }

  const hadith = resolveHadith(numArgs);
  if (!hadith) { console.error('Invalid arguments. Usage: bukhari <hadithId> [--arabic|-a|--both|-b]'); process.exit(1); }
  printHadith(hadith);
}