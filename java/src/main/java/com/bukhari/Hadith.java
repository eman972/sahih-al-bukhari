package com.bukhari;

/**
 * A single hadith from Sahih al-Bukhari.
 *
 * <p>Field names mirror the JSON schema and the JS/Python packages exactly:
 * {@code id}, {@code chapterId}, {@code arabic}, {@code english}.
 *
 * <p>Convenience shortcuts {@link #getNarrator()} and {@link #getText()}
 * avoid having to drill into {@link HadithEnglish}.
 *
 * <pre>{@code
 * Hadith h = bukhari.get(1).orElseThrow();
 * System.out.println(h.getId());        // 1
 * System.out.println(h.getNarrator());  // "Umar bin Al-Khattab"
 * System.out.println(h.getText());      // full English text
 * System.out.println(h.getArabic());    // Arabic text
 * }</pre>
 */
public class Hadith {

    private int          id;
    private int          chapterId;
    private String       arabic;
    private HadithEnglish english;

    /** 1-based global hadith ID */
    public int getId()          { return id; }

    /** ID of the chapter this hadith belongs to */
    public int getChapterId()   { return chapterId; }

    /** Full Arabic text */
    public String getArabic()   { return arabic != null ? arabic : ""; }

    /** English sub-object (narrator + text) */
    public HadithEnglish getEnglish() { return english; }

    /** Shortcut: English narrator string */
    public String getNarrator() { return english != null ? english.getNarrator() : ""; }

    /** Shortcut: English translation text */
    public String getText()     { return english != null ? english.getText()     : ""; }

    @Override
    public String toString() {
        String t = getText();
        String preview = t.length() > 80 ? t.substring(0, 80) + "…" : t;
        return "<Hadith id=" + id + " chapterId=" + chapterId + " text=\"" + preview + "\">";
    }
}
