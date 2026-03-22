package com.bukhari;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.zip.GZIPInputStream;

/**
 * Complete Sahih al-Bukhari collection — 7,277 hadiths, offline-first,
 * zero runtime dependencies beyond Gson.
 *
 * <p>This class mirrors the npm and PyPI packages API exactly (camelCase
 * method names, same semantics):
 *
 * <pre>{@code
 * // Default singleton — uses bundled data from the JAR
 * Bukhari bukhari = Bukhari.getInstance();
 *
 * // Or load a custom data file
 * Bukhari bukhari = new Bukhari(Paths.get("/path/to/bukhari.json.gz"));
 *
 * // --- Core API ---
 * bukhari.get(1);                  // Optional<Hadith>  by 1-based ID
 * bukhari.getByIndex(0);           // Hadith            by 0-based index (like JS bukhari[0])
 * bukhari.getByChapter(1);         // List<Hadith>
 * bukhari.search("prayer");        // List<Hadith>  full-text search
 * bukhari.search("prayer", 10);    // List<Hadith>  limited to 10 results
 * bukhari.getRandom();             // Hadith
 * bukhari.getHadiths();            // List<Hadith>  all hadiths (unmodifiable)
 * bukhari.getChapters();           // List<Chapter> all chapters (unmodifiable)
 * bukhari.getMetadata();           // Metadata
 * bukhari.length();                // int  total hadiths
 *
 * // --- Array-style helpers (mirror JS Array prototype / Python Bukhari) ---
 * bukhari.find(h -> h.getId() == 23);
 * bukhari.filter(h -> h.getChapterId() == 2);
 * bukhari.map(Hadith::getNarrator);
 * bukhari.forEach((h, i) -> System.out.println(i + ": " + h.getId()));
 * bukhari.slice(0, 10);
 * bukhari.stream();                // Java Stream<Hadith>
 * }</pre>
 *
 * <h2>Data source priority (first match wins)</h2>
 * <ol>
 *   <li>Custom path passed to the constructor</li>
 *   <li>Bundled {@code bukhari.json.gz} inside the JAR (classpath resource)</li>
 *   <li>Bundled {@code bukhari.json} inside the JAR (uncompressed fallback)</li>
 * </ol>
 */
public class Bukhari implements Iterable<Hadith> {

    // ── Internal data store ───────────────────────────────────────────────────

    private final List<Hadith>       hadiths;
    private final List<Chapter>      chapters;
    private final Metadata           metadata;
    private final Map<Integer, Hadith>       byId;
    private final Map<Integer, List<Hadith>> byChapter;

    private static volatile Bukhari _instance;

    // ── Constructors ──────────────────────────────────────────────────────────

    /**
     * Load from the bundled data inside the JAR.
     * This is the most common usage — equivalent to the default
     * {@code import bukhari from 'sahih-al-bukhari'} in JS.
     */
    public Bukhari() {
        BukhariData data = loadBundled();
        this.hadiths  = Collections.unmodifiableList(data.hadiths);
        this.chapters = Collections.unmodifiableList(data.chapters);
        this.metadata = data.metadata;
        this.byId      = buildById(hadiths);
        this.byChapter = buildByChapter(hadiths);
    }

    /**
     * Load from a custom local file ({@code .json} or {@code .json.gz}).
     *
     * <pre>{@code
     * Bukhari bukhari = new Bukhari(Paths.get("/my/bukhari.json.gz"));
     * }</pre>
     *
     * @param dataPath absolute or relative path to the data file
     * @throws IOException if the file cannot be read or parsed
     */
    public Bukhari(Path dataPath) throws IOException {
        BukhariData data = loadFromPath(dataPath);
        this.hadiths  = Collections.unmodifiableList(data.hadiths);
        this.chapters = Collections.unmodifiableList(data.chapters);
        this.metadata = data.metadata;
        this.byId      = buildById(hadiths);
        this.byChapter = buildByChapter(hadiths);
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    /**
     * Thread-safe singleton backed by the bundled JAR data.
     * Mirrors the default JS export and the Python singleton pattern.
     */
    public static Bukhari getInstance() {
        if (_instance == null) {
            synchronized (Bukhari.class) {
                if (_instance == null) {
                    _instance = new Bukhari();
                }
            }
        }
        return _instance;
    }

    // ── Core API — mirrors JS + Python exactly ────────────────────────────────

    /**
     * Total number of hadiths. Mirrors {@code bukhari.length} in JS/Python.
     */
    public int length() { return hadiths.size(); }

    /**
     * Get a hadith by its 1-based global ID.
     * Mirrors {@code bukhari.get(id)} in JS/Python.
     *
     * @param id 1-based hadith ID
     * @return Optional containing the hadith, or empty if not found
     */
    public Optional<Hadith> get(int id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * Get a hadith by its 0-based index in the list.
     * Mirrors {@code bukhari[0]} array access in JS/Python.
     *
     * @param index 0-based index
     * @return the hadith at that position
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public Hadith getByIndex(int index) {
        return hadiths.get(index);
    }

    /**
     * Get all hadiths belonging to a chapter.
     * Mirrors {@code bukhari.getByChapter(id)} in JS/Python.
     *
     * @param chapterId chapter ID
     * @return list of hadiths (empty list if chapter not found)
     */
    public List<Hadith> getByChapter(int chapterId) {
        return byChapter.getOrDefault(chapterId, Collections.emptyList());
    }

    /**
     * Case-insensitive full-text search across English text and narrator names.
     * Mirrors {@code bukhari.search(query)} in JS/Python.
     *
     * @param query search string
     * @return all matching hadiths
     */
    public List<Hadith> search(String query) {
        return search(query, 0);
    }

    /**
     * Case-insensitive full-text search with an optional result limit.
     * Mirrors {@code bukhari.search(query, limit)} in Python.
     *
     * @param query search string
     * @param limit max results (0 = no limit)
     * @return matching hadiths, up to {@code limit} if &gt; 0
     */
    public List<Hadith> search(String query, int limit) {
        if (query == null || query.isEmpty()) return Collections.emptyList();
        String q = query.toLowerCase(Locale.ROOT);
        Stream<Hadith> stream = hadiths.stream().filter(h ->
                h.getText().toLowerCase(Locale.ROOT).contains(q)
             || h.getNarrator().toLowerCase(Locale.ROOT).contains(q));
        if (limit > 0) stream = stream.limit(limit);
        return stream.collect(Collectors.toList());
    }

    /**
     * Return a random hadith.
     * Mirrors {@code bukhari.getRandom()} in JS/Python.
     */
    public Hadith getRandom() {
        return hadiths.get(new Random().nextInt(hadiths.size()));
    }

    // ── Collection access ─────────────────────────────────────────────────────

    /** All hadiths as an unmodifiable list. */
    public List<Hadith>  getHadiths()  { return hadiths; }

    /** All chapters as an unmodifiable list. */
    public List<Chapter> getChapters() { return chapters; }

    /** Collection metadata. */
    public Metadata      getMetadata() { return metadata; }

    // ── Array-style helpers (mirror JS Array.prototype / Python Bukhari) ──────

    /**
     * Return the first hadith matching the predicate, or empty.
     * Mirrors {@code bukhari.find(h => ...)} in JS and
     * {@code bukhari.find(lambda h: ...)} in Python.
     */
    public Optional<Hadith> find(Predicate<Hadith> predicate) {
        return hadiths.stream().filter(predicate).findFirst();
    }

    /**
     * Return all hadiths matching the predicate.
     * Mirrors {@code bukhari.filter(h => ...)} in JS/Python.
     */
    public List<Hadith> filter(Predicate<Hadith> predicate) {
        return hadiths.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Transform each hadith to another type.
     * Mirrors {@code bukhari.map(h => ...)} in JS/Python.
     */
    public <T> List<T> map(Function<Hadith, T> mapper) {
        return hadiths.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Execute an action for each hadith, receiving (hadith, index).
     * Mirrors {@code bukhari.forEach((h, i) => ...)} in JS/Python.
     */
    public void forEach(BiConsumer<Hadith, Integer> action) {
        for (int i = 0; i < hadiths.size(); i++) {
            action.accept(hadiths.get(i), i);
        }
    }

    /**
     * Return a sublist by start (inclusive) and end (exclusive) index.
     * Mirrors {@code bukhari.slice(start, end)} in JS/Python.
     */
    public List<Hadith> slice(int start, int end) {
        int s = Math.max(0, start);
        int e = Math.min(hadiths.size(), end);
        return hadiths.subList(s, e);
    }

    /**
     * Return a sublist from start to the end of the collection.
     */
    public List<Hadith> slice(int start) {
        return slice(start, hadiths.size());
    }

    /**
     * Java {@link Stream} over all hadiths — enables full Java stream API.
     *
     * <pre>{@code
     * bukhari.stream()
     *        .filter(h -> h.getChapterId() == 1)
     *        .map(Hadith::getText)
     *        .forEach(System.out::println);
     * }</pre>
     */
    public Stream<Hadith> stream() {
        return hadiths.stream();
    }

    // ── java.lang.Iterable ────────────────────────────────────────────────────

    /**
     * Makes Bukhari usable in for-each loops:
     * <pre>{@code
     * for (Hadith h : bukhari) {
     *     System.out.println(h.getId());
     * }
     * }</pre>
     */
    @Override
    public Iterator<Hadith> iterator() {
        return hadiths.iterator();
    }

    @Override
    public String toString() {
        return "<Bukhari hadiths=" + hadiths.size() + " chapters=" + chapters.size() + ">";
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private static BukhariData loadBundled() {
        // 1. Try compressed resource
        try (InputStream raw = Bukhari.class.getResourceAsStream("/bukhari.json.gz")) {
            if (raw != null) {
                return parse(new GZIPInputStream(raw));
            }
        } catch (IOException e) {
            // fall through to uncompressed
        }
        // 2. Try uncompressed resource
        try (InputStream raw = Bukhari.class.getResourceAsStream("/bukhari.json")) {
            if (raw != null) {
                return parse(raw);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bundled bukhari.json", e);
        }
        throw new RuntimeException(
            "Bundled data not found in JAR. " +
            "Expected /bukhari.json.gz or /bukhari.json in classpath resources. " +
            "If you are building from source, run `node scripts/build.mjs` first " +
            "to copy data/bukhari.json.gz into java/src/main/resources/."
        );
    }

    private static BukhariData loadFromPath(Path p) throws IOException {
        if (!Files.exists(p)) {
            throw new FileNotFoundException("Data file not found: " + p.toAbsolutePath());
        }
        try (InputStream raw = Files.newInputStream(p)) {
            InputStream stream = p.toString().endsWith(".gz")
                    ? new GZIPInputStream(raw)
                    : raw;
            return parse(stream);
        }
    }

    private static BukhariData parse(InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in, "UTF-8")) {
            Gson gson = new Gson();
            BukhariData data = gson.fromJson(reader, BukhariData.class);
            if (data == null || data.hadiths == null) {
                throw new IOException("Invalid data format: missing 'hadiths' key.");
            }
            return data;
        }
    }

    // ── Index builders ────────────────────────────────────────────────────────

    private static Map<Integer, Hadith> buildById(List<Hadith> hadiths) {
        Map<Integer, Hadith> map = new HashMap<>(hadiths.size() * 2);
        for (Hadith h : hadiths) map.put(h.getId(), h);
        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, List<Hadith>> buildByChapter(List<Hadith> hadiths) {
        Map<Integer, List<Hadith>> map = new LinkedHashMap<>();
        for (Hadith h : hadiths) {
            map.computeIfAbsent(h.getChapterId(), k -> new ArrayList<>()).add(h);
        }
        // wrap each list as unmodifiable
        Map<Integer, List<Hadith>> result = new LinkedHashMap<>(map.size() * 2);
        map.forEach((k, v) -> result.put(k, Collections.unmodifiableList(v)));
        return Collections.unmodifiableMap(result);
    }

    // ── Internal GSON target ──────────────────────────────────────────────────

    private static class BukhariData {
        Metadata      metadata;
        List<Chapter> chapters;
        List<Hadith>  hadiths;
    }
}
