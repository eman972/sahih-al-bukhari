// Browser & Node compatible — no fs/path/url imports
// Data is injected at build time via the Node entry (bin/index.js)

function createBukhari(bukhariData) {
  const hadiths = bukhariData.hadiths;

  const books = {};
  hadiths.forEach(hadith => {
    const bookId = hadith.bookId;
    if (!books[bookId]) books[bookId] = [];
    books[bookId].push(hadith);
  });

  return new Proxy(hadiths, {
    get(target, prop) {
      if (!isNaN(prop)) return target[parseInt(prop)];
      if (prop in target) return target[prop];
      switch (prop) {
        case 'books':    return books;
        case 'metadata': return bukhariData.metadata;
        case 'chapters': return bukhariData.chapters;
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
      return ['length', ...Array.from({length: target.length}, (_, i) => i.toString()),
        'books', 'metadata', 'chapters', 'getByBook', 'getByChapter', 'search', 'getRandom'];
    }
  });
}

export { createBukhari };
export default createBukhari;