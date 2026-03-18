// Browser/bundler entry — webpack, Vite, React, Vue, etc.
// Uses require() for JSON so no import assertions needed (works with all babel versions)
// Usage:
//   import bukhari from 'sahih-al-bukhari'
//   import { Bukhari } from 'sahih-al-bukhari'

// eslint-disable-next-line @typescript-eslint/no-var-requires
const bukhariData = require('./bin/bukhari.json');

export class Bukhari {
  constructor(data) {
    const hadiths = data.hadiths;
    const books   = {};
    hadiths.forEach(h => {
      if (!books[h.bookId]) books[h.bookId] = [];
      books[h.bookId].push(h);
    });
    return new Proxy(hadiths, {
      get(target, prop) {
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

const bukhari = new Bukhari(bukhariData);
export default bukhari;