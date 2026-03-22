package com.bukhari;

/**
 * A chapter (book) within Sahih al-Bukhari.
 *
 * <pre>{@code
 * Chapter c = bukhari.getChapters().get(0);
 * System.out.println(c.getId());      // 1
 * System.out.println(c.getEnglish()); // "Revelation"
 * System.out.println(c.getArabic());  // Arabic chapter title
 * }</pre>
 */
public class Chapter {

    private int    id;
    private String arabic;
    private String english;

    /** Chapter ID */
    public int getId()          { return id; }

    /** Arabic chapter title */
    public String getArabic()   { return arabic  != null ? arabic  : ""; }

    /** English chapter title */
    public String getEnglish()  { return english != null ? english : ""; }

    @Override
    public String toString() {
        return "<Chapter id=" + id + " english=\"" + getEnglish() + "\">";
    }
}
