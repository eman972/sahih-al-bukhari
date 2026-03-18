export interface HadithEnglish { narrator: string; text: string; }
export interface Hadith { id: number; chapterId: number; bookId: number; arabic: string; english: HadithEnglish; }
export interface Chapter { id: number; bookId: number; arabic: string; english: string; }
export interface Metadata { id: number; length: number; arabic: { title: string; author: string; introduction: string }; english: { title: string; author: string; introduction: string }; }
export interface BukhariData { metadata: Metadata; chapters: Chapter[]; hadiths: Hadith[]; }

export interface BukhariInstance extends ArrayLike<Hadith> {
  [index: number]: Hadith;
  readonly length: number;
  readonly books: Record<number, Hadith[]>;
  readonly metadata: Metadata;
  readonly chapters: Chapter[];
  getByBook(bookId: number): Hadith[];
  getByChapter(chapterId: number): Hadith[];
  search(query: string): Hadith[];
  getRandom(): Hadith;
  find(predicate: (h: Hadith) => boolean): Hadith | undefined;
  filter(predicate: (h: Hadith) => boolean): Hadith[];
  map<T>(cb: (h: Hadith, i: number) => T): T[];
  forEach(cb: (h: Hadith, i: number) => void): void;
  slice(start?: number, end?: number): Hadith[];
}

export declare class Bukhari {
  constructor(data: BukhariData);
}

// Default export is a thenable proxy — use `await bukhari` to get BukhariInstance
declare const bukhari: Promise<BukhariInstance> & Partial<BukhariInstance>;
export default bukhari;
