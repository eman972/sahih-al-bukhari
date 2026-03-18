// node build-browser.mjs
// Splits bukhari.json into per-chapter chunk files for lazy loading
// Also generates index.browser.js (lightweight, no data baked in)

import fs   from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dataPath  = path.join(__dirname, 'bin', 'bukhari.json');
const chunksDir = path.join(__dirname, 'chunks');

console.log('Reading bukhari.json...');
const data = JSON.parse(fs.readFileSync(dataPath, 'utf8'));

// ── 1. Write chunks/meta.json (metadata + chapters, no hadiths — tiny) ───────
if (!fs.existsSync(chunksDir)) fs.mkdirSync(chunksDir);

const meta = {
  metadata: data.metadata,
  chapters: data.chapters,
  totalHadiths: data.hadiths.length,
};
fs.writeFileSync(path.join(chunksDir, 'meta.json'), JSON.stringify(meta), 'utf8');
console.log('Written chunks/meta.json');

// ── 2. Write chunks/chapter-{id}.json (one file per chapter) ─────────────────
const byChapter = {};
data.hadiths.forEach(h => {
  if (!byChapter[h.chapterId]) byChapter[h.chapterId] = [];
  byChapter[h.chapterId].push(h);
});

let count = 0;
for (const [chapterId, hadiths] of Object.entries(byChapter)) {
  fs.writeFileSync(
    path.join(chunksDir, `chapter-${chapterId}.json`),
    JSON.stringify(hadiths),
    'utf8'
  );
  count++;
}
console.log(`Written ${count} chapter chunk files to chunks/`);

// ── 3. Write index.browser.js (lightweight — no data inside) ─────────────────
const browserEntry = `// AUTO-GENERATED — do not edit manually. Run: node build-browser.mjs
//
// Lightweight browser entry — loads data lazily per chapter
// Works in: React, Vue, Vite, webpack, Next.js, any bundler
// No fs/path/require/import-assertions needed
//
// Usage:
//   import bukhari from 'sahih-al-bukhari'          // default lazy instance
//   import { Bukhari } from 'sahih-al-bukhari'       // class for custom data

export class Bukhari {
  constructor(data) {
    if (!data) throw new Error('[sahih-al-bukhari] Pass bukhariData to constructor, or use the default export.');
    const hadiths = data.hadiths || data;
    const books   = {};
    hadiths.forEach(h => {
      if (!books[h.bookId]) books[h.bookId] = [];
      books[h.bookId].push(h);
    });
    this._hadiths  = hadiths;
    this._books    = books;
    this._metadata = data.metadata;
    this._chapters = data.chapters;
    return new Proxy(hadiths, {
      get: (target, prop) => {
        if (!isNaN(prop))   return target[parseInt(prop)];
        if (prop in target) return target[prop];
        switch (prop) {
          case 'books':        return books;
          case 'metadata':     return data.metadata;
          case 'chapters':     return data.chapters;
          case 'getByBook':    return (id) => books[id] || [];
          case 'getByChapter': return (id) => hadiths.filter(h => h.chapterId === id);
          case 'search':       return (q)  => hadiths.filter(h =>
            h.english?.text?.toLowerCase().includes(q.toLowerCase()) ||
            h.english?.narrator?.toLowerCase().includes(q.toLowerCase())
          );
          case 'getRandom': return () => hadiths[Math.floor(Math.random() * hadiths.length)];
          case 'length':    return target.length;
          default:          return target[prop];
        }
      },
      ownKeys: (target) => [
        'length',
        ...Array.from({ length: target.length }, (_, i) => String(i)),
        'books', 'metadata', 'chapters', 'getByBook', 'getByChapter', 'search', 'getRandom'
      ]
    });
  }
}

export class LazyBukhari {
  static async load(chapterIds, baseUrl = 'https://cdn.jsdelivr.net/npm/sahih-al-bukhari/chunks') {
    const results = await Promise.all(
      chapterIds.map(id => fetch(\`\${baseUrl}/chapter-\${id}.json\`).then(r => r.json()))
    );
    const hadiths = results.flat();
    const metaRes = await fetch(\`\${baseUrl}/meta.json\`);
    const meta    = await metaRes.json();
    return new Bukhari({ hadiths, metadata: meta.metadata, chapters: meta.chapters });
  }

  static async loadAll(baseUrl = 'https://cdn.jsdelivr.net/npm/sahih-al-bukhari/chunks') {
    const meta = await fetch(\`\${baseUrl}/meta.json\`).then(r => r.json());
    const chapterIds = meta.chapters.map(c => c.id);
    return LazyBukhari.load(chapterIds, baseUrl);
  }
}

class _LazyProxy {
  constructor() {
    this._instance  = null;
    this._loading   = null;
    this._warned    = false;

    return new Proxy(this, {
      get: (self, prop) => {
        if (prop === 'ready') {
          return self._loading || (self._loading = LazyBukhari.loadAll().then(b => { self._instance = b; return b; }));
        }
        if (!self._instance && !self._warned) {
          self._warned = true;
          console.warn('[sahih-al-bukhari] Synchronous access before data loaded. Use \`await bukhari.ready\` first.');
          self._loading = self._loading || LazyBukhari.loadAll().then(b => { self._instance = b; });
          return undefined;
        }
        if (self._instance) {
          const val = self._instance[prop];
          return typeof val === 'function' ? val.bind(self._instance) : val;
        }
        return undefined;
      }
    });
  }
}

export default new _LazyProxy();
`;

fs.writeFileSync(path.join(__dirname, 'index.browser.js'), browserEntry, 'utf8');
console.log('Written index.browser.js (' + (browserEntry.length / 1024).toFixed(1) + ' KB — lightweight!)');
console.log('');
console.log('Done! Summary:');
console.log('  index.browser.js  : ' + (browserEntry.length / 1024).toFixed(1) + ' KB');
console.log('  chunks/meta.json  : metadata + chapters only');
console.log('  chunks/chapter-*  : ' + count + ' files, loaded on demand');