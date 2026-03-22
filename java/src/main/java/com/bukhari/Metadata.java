package com.bukhari;

import java.util.Collections;
import java.util.Map;

/**
 * Collection-level metadata for Sahih al-Bukhari.
 *
 * <pre>{@code
 * Metadata m = bukhari.getMetadata();
 * System.out.println(m.getLength());               // 7277
 * System.out.println(m.getEnglish().get("title")); // "Sahih al-Bukhari"
 * System.out.println(m.getEnglish().get("author")); // "Muhammad al-Bukhari"
 * }</pre>
 */
public class Metadata {

    private int                 id;
    private int                 length;
    private Map<String, String> arabic;
    private Map<String, String> english;

    /** Metadata record ID */
    public int getId()     { return id; }

    /** Total number of hadiths in the collection */
    public int getLength() { return length; }

    /**
     * Arabic metadata map with keys: {@code title}, {@code author},
     * {@code introduction}
     */
    public Map<String, String> getArabic() {
        return arabic != null ? Collections.unmodifiableMap(arabic) : Collections.emptyMap();
    }

    /**
     * English metadata map with keys: {@code title}, {@code author},
     * {@code introduction}
     */
    public Map<String, String> getEnglish() {
        return english != null ? Collections.unmodifiableMap(english) : Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "<Metadata title=\"" + getEnglish().get("title")
                + "\" author=\"" + getEnglish().get("author") + "\">";
    }
}
