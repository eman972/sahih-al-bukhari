import bukhari from './index.js';

console.log('=== Testing Sahih al-Bukhari Package ===\n');

// Test 1: Basic array access
console.log('1. Testing array access:');
console.log(`Total hadiths: ${bukhari.length}`);
console.log(`First hadith narrator: ${bukhari[0]?.english?.narrator}`);
console.log(`Hadith #23 narrator: ${bukhari[23]?.english?.narrator}`);
console.log(`bukhari[23][0] test:`, bukhari[23]?.bookId || 'Property access test');

// Test 2: Book access
console.log('\n2. Testing book access:');
const book1 = bukhari.getByBook(1);
console.log(`Book 1 has ${book1.length} hadiths`);
console.log(`First hadith in Book 1: ${book1[0]?.english?.narrator}`);

// Test 3: Search functionality
console.log('\n3. Testing search:');
const prayerHadiths = bukhari.search('prayer');
console.log(`Found ${prayerHadiths.length} hadiths about prayer`);

const anasHadiths = bukhari.search('Anas');
console.log(`Found ${anasHadiths.length} hadiths narrated by Anas`);

// Test 4: Random hadith
console.log('\n4. Testing random hadith:');
const randomHadith = bukhari.getRandom();
console.log(`Random hadith narrator: ${randomHadith?.english?.narrator}`);

// Test 5: Metadata access
console.log('\n5. Testing metadata:');
console.log(`Collection title: ${bukhari.metadata?.english?.title}`);
console.log(`Author: ${bukhari.metadata?.english?.author}`);
console.log(`Total chapters: ${bukhari.chapters?.length}`);

// Test 6: Chapter access
console.log('\n6. Testing chapter access:');
const chapter1 = bukhari.getByChapter(1);
console.log(`Chapter 1 has ${chapter1.length} hadiths`);

console.log('\n=== All tests completed successfully! ===');
