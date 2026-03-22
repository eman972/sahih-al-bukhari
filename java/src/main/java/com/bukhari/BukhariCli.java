package com.bukhari;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command-line interface for Sahih al-Bukhari — mirrors the Node.js and
 * Python CLIs exactly.
 *
 * <h2>Usage</h2>
 * <pre>
 *   bukhari &lt;id&gt;                  # get hadith by ID
 *   bukhari &lt;id&gt; -a               # show Arabic only
 *   bukhari &lt;id&gt; -b               # show both Arabic + English
 *   bukhari &lt;id1&gt; &lt;id2&gt; ...      # show multiple hadiths
 *   bukhari --search "prayer"     # full-text search
 *   bukhari --search "prayer" --all  # show all results (default: 10)
 *   bukhari --chapter &lt;id&gt;        # all hadiths in a chapter
 *   bukhari --random              # random hadith
 *   bukhari --random -b           # random hadith, Arabic + English
 *   bukhari --count               # total hadith count
 *   bukhari --version             # print version
 *   bukhari --help                # show help
 * </pre>
 *
 * <p>Build an executable fat-JAR with Maven:
 * <pre>
 *   mvn package -P fat-jar
 *   java -jar target/sahih-al-bukhari-3.1.5-fat.jar --random
 * </pre>
 */
public class BukhariCli {

    static final String VERSION = "3.1.5";

    // ── ANSI colours ──────────────────────────────────────────────────────────
    private static final String RESET   = "\u001b[0m";
    private static final String BOLD    = "\u001b[1m";
    private static final String DIM     = "\u001b[2m";
    private static final String GREEN   = "\u001b[32m";
    private static final String YELLOW  = "\u001b[33m";
    private static final String CYAN    = "\u001b[36m";
    private static final String MAGENTA = "\u001b[35m";
    private static final String GRAY    = "\u001b[90m";
    private static final String RED     = "\u001b[31m";

    private static String c(String col, String t) { return col + t + RESET; }
    private static String bold(String t)    { return c(BOLD,    t); }
    private static String cyan(String t)    { return c(CYAN,    t); }
    private static String yellow(String t)  { return c(YELLOW,  t); }
    private static String magenta(String t) { return c(MAGENTA, t); }
    private static String gray(String t)    { return c(GRAY,    t); }
    private static String red(String t)     { return c(RED,     t); }
    private static String dim(String t)     { return c(DIM,     t); }

    private static final String DIV  = gray("─".repeat(60));
    private static final String DIV2 = gray("═".repeat(60));

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        if (args.length == 0) { printHelp(); System.exit(0); }

        // Parse flags
        boolean showArabic  = false;
        boolean showBoth    = false;
        boolean showAll     = false;
        boolean doRandom    = false;
        boolean doVersion   = false;
        boolean doHelp      = false;
        boolean doCount     = false;
        String  searchQuery = null;
        Integer chapterId   = null;
        List<Integer> ids   = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a": case "--arabic":  showArabic = true;  break;
                case "-b": case "--both":    showBoth   = true;  break;
                case "--all":               showAll    = true;  break;
                case "-r": case "--random":  doRandom   = true;  break;
                case "-v": case "--version": doVersion  = true;  break;
                case "-h": case "--help":    doHelp     = true;  break;
                case "--count":             doCount    = true;  break;
                case "-s": case "--search":
                    if (i + 1 < args.length) searchQuery = args[++i];
                    break;
                case "-c": case "--chapter":
                    if (i + 1 < args.length) {
                        try { chapterId = Integer.parseInt(args[++i]); }
                        catch (NumberFormatException e) { err("Invalid chapter id: " + args[i]); }
                    }
                    break;
                default:
                    try { ids.add(Integer.parseInt(args[i])); }
                    catch (NumberFormatException ignored) {
                        // Unknown flag — silently skip
                    }
            }
        }

        boolean showEnglish = !showArabic || showBoth;
        if (showBoth) { showArabic = true; showEnglish = true; }

        if (doHelp)    { printHelp();    System.exit(0); }
        if (doVersion) { printVersion(); System.exit(0); }

        // Load data
        System.out.print(gray("  Loading…") + "\r");
        Bukhari bukhari;
        try {
            bukhari = Bukhari.getInstance();
        } catch (Exception e) {
            err("Failed to load data: " + e.getMessage());
            return;
        }
        System.out.print("                    \r");

        Map<Integer, Chapter> chaptersMap = new LinkedHashMap<>();
        for (Chapter ch : bukhari.getChapters()) chaptersMap.put(ch.getId(), ch);

        // --count
        if (doCount) {
            System.out.println(bukhari.length());
            System.exit(0);
        }

        // --random
        if (doRandom) {
            printHadith(bukhari.getRandom(), chaptersMap, showArabic, showEnglish, null);
            System.exit(0);
        }

        // --chapter
        if (chapterId != null) {
            List<Hadith> ch = bukhari.getByChapter(chapterId);
            Chapter chapter = chaptersMap.get(chapterId);
            if (ch.isEmpty()) {
                err("No chapter found with id " + chapterId);
                return;
            }
            System.out.println();
            System.out.println(DIV2);
            String hdr = bold(cyan("  Chapter " + chapterId));
            if (chapter != null) hdr += gray(" — ") + yellow(chapter.getEnglish());
            System.out.println(hdr);
            if (chapter != null && !chapter.getArabic().isEmpty())
                System.out.println("  " + magenta(chapter.getArabic()));
            System.out.println(gray("  " + ch.size() + " hadiths"));
            System.out.println(DIV2);
            for (Hadith h : ch) printHadith(h, chaptersMap, showArabic, showEnglish, null);
            System.exit(0);
        }

        // --search
        if (searchQuery != null) {
            long t0 = System.currentTimeMillis();
            List<Hadith> results = bukhari.search(searchQuery);
            long ms = System.currentTimeMillis() - t0;

            System.out.println();
            System.out.println(DIV2);
            System.out.println("  " + bold(cyan("Search: ")) + yellow("\"" + searchQuery + "\""));
            System.out.println("  " + gray(results.size() + " results — " + ms + "ms"));
            System.out.println(DIV2);

            List<Hadith> toShow = showAll ? results : results.stream().limit(10).collect(Collectors.toList());
            for (Hadith h : toShow) printHadith(h, chaptersMap, showArabic, showEnglish, searchQuery);

            if (!showAll && results.size() > 10) {
                System.out.println("  " + dim("Showing 10 of " + results.size() + " — use --all to see all"));
                System.out.println();
            }
            System.exit(0);
        }

        // Positional IDs: bukhari 1  or  bukhari 1 2 3
        if (!ids.isEmpty()) {
            for (int id : ids) {
                Optional<Hadith> h = bukhari.get(id);
                if (h.isPresent()) {
                    printHadith(h.get(), chaptersMap, showArabic, showEnglish, null);
                } else {
                    System.out.println(red("  Hadith " + id + " not found."));
                }
            }
            System.exit(0);
        }

        printHelp();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private static void printHadith(Hadith h, Map<Integer, Chapter> chaptersMap,
                                    boolean showArabic, boolean showEnglish,
                                    String highlightTerm) {
        Chapter chapter = chaptersMap.get(h.getChapterId());
        System.out.println();
        System.out.println(DIV2);

        if (showArabic && !showEnglish) {
            String hdr = "  " + bold(magenta("حديث #" + h.getId()))
                       + gray("  |  باب: ") + magenta(String.valueOf(h.getChapterId()));
            if (chapter != null && !chapter.getArabic().isEmpty())
                hdr += gray(" — ") + magenta(chapter.getArabic());
            System.out.println(hdr);
        } else {
            String hdr = "  " + bold(cyan("Hadith #" + h.getId()))
                       + gray("  |  Chapter: ") + cyan(String.valueOf(h.getChapterId()));
            if (chapter != null && !chapter.getEnglish().isEmpty())
                hdr += gray(" — ") + yellow(chapter.getEnglish());
            System.out.println(hdr);
        }
        System.out.println(DIV2);

        if (showEnglish) {
            if (!h.getNarrator().isEmpty())
                System.out.println("  " + bold(yellow("Narrator: ")) + magenta(h.getNarrator()));
            if (!h.getText().isEmpty()) {
                System.out.println();
                String text = highlightTerm != null ? highlight(h.getText(), highlightTerm) : h.getText();
                System.out.println(wrap(text, 72, "  "));
            }
        }

        if (showArabic) {
            if (showEnglish) System.out.println("\n" + DIV);
            if (!h.getArabic().isEmpty()) {
                System.out.println();
                System.out.println("  " + magenta(h.getArabic()));
            }
        }

        System.out.println();
        System.out.println(DIV2);
        System.out.println();
    }

    private static String wrap(String text, int width, String indent) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() + 1 > width) {
                if (line.length() > 0) {
                    sb.append(indent).append(line.toString().trim()).append("\n");
                    line = new StringBuilder();
                }
            }
            if (line.length() > 0) line.append(" ");
            line.append(word);
        }
        if (line.length() > 0) sb.append(indent).append(line.toString().trim());
        return sb.toString();
    }

    private static String highlight(String text, String term) {
        if (term == null || term.isEmpty()) return text;
        return text.replaceAll(
            "(?i)(" + java.util.regex.Pattern.quote(term) + ")",
            BOLD + YELLOW + "$1" + RESET
        );
    }

    private static void printVersion() {
        System.out.println();
        System.out.println("  " + bold(cyan("sahih-al-bukhari")) + " " + gray("v" + VERSION));
        System.out.println();
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("  " + bold(cyan("sahih-al-bukhari")) + " " + gray("v" + VERSION));
        System.out.println("  " + gray("Complete Sahih al-Bukhari — 7,277 hadiths"));
        System.out.println();
        System.out.println("  " + bold("Usage:"));
        System.out.println("    " + cyan("bukhari") + " " + yellow("<id>")                    + "                 " + gray("Get hadith by ID"));
        System.out.println("    " + cyan("bukhari") + " " + yellow("<id>") + " -a"             + "              " + gray("Arabic text only"));
        System.out.println("    " + cyan("bukhari") + " " + yellow("<id>") + " -b"             + "              " + gray("Arabic + English"));
        System.out.println("    " + cyan("bukhari") + " " + yellow("<id1> <id2> ...")          + "         " + gray("Multiple hadiths"));
        System.out.println("    " + cyan("bukhari") + " --search " + yellow("<query>")         + "        " + gray("Full-text search"));
        System.out.println("    " + cyan("bukhari") + " --search " + yellow("<query>") + " --all  " + gray("Show all results"));
        System.out.println("    " + cyan("bukhari") + " --chapter " + yellow("<id>")           + "        " + gray("All hadiths in a chapter"));
        System.out.println("    " + cyan("bukhari") + " --random"                              + "              " + gray("Random hadith"));
        System.out.println("    " + cyan("bukhari") + " --count"                               + "               " + gray("Total hadith count"));
        System.out.println("    " + cyan("bukhari") + " --version"                             + "             " + gray("Print version"));
        System.out.println();
        System.out.println("  " + bold("Flags:"));
        System.out.println("    " + yellow("-a, --arabic") + "   Show Arabic text only");
        System.out.println("    " + yellow("-b, --both")   + "     Show Arabic + English");
        System.out.println("    " + yellow("--all")        + "          Show all search results (default: 10)");
        System.out.println();
        System.out.println("  " + bold("Examples:"));
        System.out.println("    bukhari 1");
        System.out.println("    bukhari 2345 -a");
        System.out.println("    bukhari 23 34 100");
        System.out.println("    bukhari --search \"prayer\"");
        System.out.println("    bukhari --search \"fasting\" --all");
        System.out.println("    bukhari --chapter 5");
        System.out.println("    bukhari --random -b");
        System.out.println();
    }

    private static void err(String msg) {
        System.err.println(red("  ✗ " + msg));
        System.exit(1);
    }
}
