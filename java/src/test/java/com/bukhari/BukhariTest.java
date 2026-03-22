package com.bukhari;

import org.junit.jupiter.api.*;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Bukhari Java library.
 *
 * Covers every public method and mirrors the test expectations of the
 * npm and PyPI packages.
 *
 * NOTE: Tests require bundled bukhari.json.gz to be present in
 *       src/main/resources/ — run `node scripts/build.mjs` first
 *       if the file is missing.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BukhariTest {

    static Bukhari bukhari;

    @BeforeAll
    static void setup() {
        bukhari = Bukhari.getInstance();
    }

    // ── Singleton / loading ───────────────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("Singleton returns same instance")
    void testSingleton() {
        Bukhari a = Bukhari.getInstance();
        Bukhari b = Bukhari.getInstance();
        assertSame(a, b, "getInstance() must return same object");
    }

    // ── length() ──────────────────────────────────────────────────────────────

    @Test @Order(2)
    @DisplayName("length() returns 7277 hadiths")
    void testLength() {
        assertEquals(7277, bukhari.length(), "Expected 7277 hadiths in the full collection");
    }

    // ── get(id) ───────────────────────────────────────────────────────────────

    @Test @Order(3)
    @DisplayName("get(1) returns first hadith")
    void testGetFirst() {
        Optional<Hadith> h = bukhari.get(1);
        assertTrue(h.isPresent(), "Hadith with id=1 must exist");
        assertEquals(1, h.get().getId());
    }

    @Test @Order(4)
    @DisplayName("get(7277) returns last hadith")
    void testGetLast() {
        Optional<Hadith> h = bukhari.get(7277);
        assertTrue(h.isPresent(), "Hadith with id=7277 must exist");
        assertEquals(7277, h.get().getId());
    }

    @Test @Order(5)
    @DisplayName("get() with non-existent ID returns empty Optional")
    void testGetMissing() {
        assertFalse(bukhari.get(999999).isPresent(), "get(999999) should return empty");
        assertFalse(bukhari.get(0).isPresent(),      "get(0) should return empty (IDs are 1-based)");
        assertFalse(bukhari.get(-1).isPresent(),     "get(-1) should return empty");
    }

    // ── getByIndex(i) ─────────────────────────────────────────────────────────

    @Test @Order(6)
    @DisplayName("getByIndex(0) mirrors bukhari[0] in JS/Python")
    void testGetByIndex() {
        Hadith h = bukhari.getByIndex(0);
        assertNotNull(h);
        assertEquals(bukhari.get(h.getId()).orElse(null), h);
    }

    @Test @Order(7)
    @DisplayName("getByIndex() throws on out-of-range index")
    void testGetByIndexOutOfRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> bukhari.getByIndex(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bukhari.getByIndex(bukhari.length()));
    }

    // ── getByChapter() ────────────────────────────────────────────────────────

    @Test @Order(8)
    @DisplayName("getByChapter(1) returns non-empty list")
    void testGetByChapter() {
        List<Hadith> ch = bukhari.getByChapter(1);
        assertFalse(ch.isEmpty(), "Chapter 1 must have hadiths");
    }

    @Test @Order(9)
    @DisplayName("Every hadith in getByChapter has correct chapterId")
    void testGetByChapterConsistency() {
        List<Hadith> ch = bukhari.getByChapter(3);
        for (Hadith h : ch) {
            assertEquals(3, h.getChapterId(),
                "All hadiths returned by getByChapter(3) must have chapterId=3");
        }
    }

    @Test @Order(10)
    @DisplayName("getByChapter() with non-existent chapter returns empty list")
    void testGetByChapterMissing() {
        List<Hadith> ch = bukhari.getByChapter(99999);
        assertTrue(ch.isEmpty(), "Non-existent chapter should return empty list");
    }

    // ── search() ──────────────────────────────────────────────────────────────

    @Test @Order(11)
    @DisplayName("search() returns results for 'prayer'")
    void testSearch() {
        List<Hadith> results = bukhari.search("prayer");
        assertFalse(results.isEmpty(), "search('prayer') must return results");
    }

    @Test @Order(12)
    @DisplayName("search() is case-insensitive")
    void testSearchCaseInsensitive() {
        List<Hadith> lower = bukhari.search("prayer");
        List<Hadith> upper = bukhari.search("PRAYER");
        List<Hadith> mixed = bukhari.search("Prayer");
        assertEquals(lower.size(), upper.size(), "search must be case-insensitive");
        assertEquals(lower.size(), mixed.size(), "search must be case-insensitive");
    }

    @Test @Order(13)
    @DisplayName("search() matches narrator names")
    void testSearchNarrator() {
        // Many hadiths have "Umar" as narrator
        List<Hadith> results = bukhari.search("Umar");
        assertFalse(results.isEmpty(), "search('Umar') should match narrator names");
    }

    @Test @Order(14)
    @DisplayName("search() returns empty list for garbage query")
    void testSearchNoMatch() {
        List<Hadith> results = bukhari.search("xyzzy_no_match_12345");
        assertTrue(results.isEmpty(), "Garbage search should return empty list");
    }

    @Test @Order(15)
    @DisplayName("search(query, limit) respects the limit")
    void testSearchWithLimit() {
        List<Hadith> all     = bukhari.search("prayer");
        List<Hadith> limited = bukhari.search("prayer", 5);
        assertTrue(all.size() > 5, "search('prayer') should return more than 5 results");
        assertEquals(5, limited.size(), "search with limit=5 should return exactly 5");
    }

    @Test @Order(16)
    @DisplayName("search(query, 0) returns all matches (no limit)")
    void testSearchLimitZero() {
        List<Hadith> all     = bukhari.search("prayer");
        List<Hadith> noLimit = bukhari.search("prayer", 0);
        assertEquals(all.size(), noLimit.size(), "limit=0 should mean no limit");
    }

    // ── getRandom() ───────────────────────────────────────────────────────────

    @Test @Order(17)
    @DisplayName("getRandom() returns a valid hadith")
    void testGetRandom() {
        Hadith h = bukhari.getRandom();
        assertNotNull(h);
        assertTrue(h.getId() >= 1 && h.getId() <= 7277, "Random hadith ID should be in range");
    }

    @Test @Order(18)
    @DisplayName("getRandom() returns different hadiths over multiple calls")
    void testGetRandomVariety() {
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < 20; i++) ids.add(bukhari.getRandom().getId());
        assertTrue(ids.size() > 1, "20 random calls should not all return the same hadith");
    }

    // ── getMetadata() ─────────────────────────────────────────────────────────

    @Test @Order(19)
    @DisplayName("getMetadata() returns non-null metadata")
    void testMetadata() {
        Metadata m = bukhari.getMetadata();
        assertNotNull(m);
        assertNotNull(m.getEnglish());
        assertFalse(m.getEnglish().isEmpty());
    }

    @Test @Order(20)
    @DisplayName("metadata.length matches bukhari.length()")
    void testMetadataLength() {
        assertEquals(bukhari.length(), bukhari.getMetadata().getLength(),
            "metadata.length must match actual hadith count");
    }

    // ── getChapters() ─────────────────────────────────────────────────────────

    @Test @Order(21)
    @DisplayName("getChapters() returns non-empty list")
    void testChapters() {
        List<Chapter> chapters = bukhari.getChapters();
        assertFalse(chapters.isEmpty(), "chapters list must not be empty");
    }

    @Test @Order(22)
    @DisplayName("Every chapter has a non-empty English title")
    void testChapterEnglishTitles() {
        for (Chapter ch : bukhari.getChapters()) {
            assertFalse(ch.getEnglish().isEmpty(),
                "Chapter " + ch.getId() + " must have an English title");
        }
    }

    // ── getHadiths() ─────────────────────────────────────────────────────────

    @Test @Order(23)
    @DisplayName("getHadiths() list is unmodifiable")
    void testHadithsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
            () -> bukhari.getHadiths().add(null),
            "getHadiths() should return an unmodifiable list");
    }

    // ── find() ───────────────────────────────────────────────────────────────

    @Test @Order(24)
    @DisplayName("find() returns first match")
    void testFind() {
        Optional<Hadith> found = bukhari.find(h -> h.getChapterId() == 5);
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getChapterId());
    }

    @Test @Order(25)
    @DisplayName("find() returns empty when predicate never matches")
    void testFindNoMatch() {
        Optional<Hadith> found = bukhari.find(h -> h.getId() == -99);
        assertFalse(found.isPresent());
    }

    // ── filter() ─────────────────────────────────────────────────────────────

    @Test @Order(26)
    @DisplayName("filter() returns all matches")
    void testFilter() {
        List<Hadith> filtered = bukhari.filter(h -> h.getChapterId() == 1);
        for (Hadith h : filtered) assertEquals(1, h.getChapterId());
        assertFalse(filtered.isEmpty());
    }

    // ── map() ─────────────────────────────────────────────────────────────────

    @Test @Order(27)
    @DisplayName("map() transforms all hadiths")
    void testMap() {
        List<Integer> ids = bukhari.map(Hadith::getId);
        assertEquals(bukhari.length(), ids.size(), "map() must return one entry per hadith");
    }

    // ── forEach() ────────────────────────────────────────────────────────────

    @Test @Order(28)
    @DisplayName("forEach() visits every hadith with correct index")
    void testForEach() {
        int[] count = {0};
        bukhari.forEach((h, i) -> {
            assertEquals(count[0], (int) i, "index must increment correctly");
            count[0]++;
        });
        assertEquals(bukhari.length(), count[0], "forEach must visit all hadiths");
    }

    // ── slice() ───────────────────────────────────────────────────────────────

    @Test @Order(29)
    @DisplayName("slice(0, 10) returns exactly 10 hadiths")
    void testSlice() {
        List<Hadith> s = bukhari.slice(0, 10);
        assertEquals(10, s.size());
    }

    @Test @Order(30)
    @DisplayName("slice(start) returns hadiths from start to end")
    void testSliceOneArg() {
        List<Hadith> s = bukhari.slice(bukhari.length() - 5);
        assertEquals(5, s.size());
    }

    // ── stream() ─────────────────────────────────────────────────────────────

    @Test @Order(31)
    @DisplayName("stream() enables Java stream operations")
    void testStream() {
        long count = bukhari.stream()
                            .filter(h -> h.getText().toLowerCase().contains("prayer"))
                            .count();
        assertTrue(count > 0, "stream() filter on 'prayer' must find results");
    }

    // ── Iterable ─────────────────────────────────────────────────────────────

    @Test @Order(32)
    @DisplayName("Bukhari is iterable in a for-each loop")
    void testIterable() {
        int count = 0;
        for (Hadith h : bukhari) {
            assertNotNull(h);
            count++;
        }
        assertEquals(bukhari.length(), count, "for-each must visit all hadiths");
    }

    // ── Hadith model fields ───────────────────────────────────────────────────

    @Test @Order(33)
    @DisplayName("Hadith fields are all populated")
    void testHadithFields() {
        Hadith h = bukhari.get(1).orElseThrow();
        assertTrue(h.getId() > 0,          "id must be positive");
        assertTrue(h.getChapterId() > 0,   "chapterId must be positive");
        assertFalse(h.getText().isEmpty(),     "text must not be empty");
        assertFalse(h.getArabic().isEmpty(),   "arabic must not be empty");
    }

    @Test @Order(34)
    @DisplayName("HadithEnglish narrator shortcut works")
    void testNarratorShortcut() {
        // Find a hadith known to have a narrator
        Optional<Hadith> h = bukhari.find(hh -> !hh.getNarrator().isEmpty());
        assertTrue(h.isPresent(), "At least one hadith must have a narrator");
        assertEquals(h.get().getEnglish().getNarrator(), h.get().getNarrator());
    }

    // ── toString() ───────────────────────────────────────────────────────────

    @Test @Order(35)
    @DisplayName("toString() produces readable output")
    void testToString() {
        String s = bukhari.toString();
        assertTrue(s.contains("Bukhari"), "toString must contain 'Bukhari'");
        assertTrue(s.contains("7277"),    "toString must mention hadith count");
    }
}
