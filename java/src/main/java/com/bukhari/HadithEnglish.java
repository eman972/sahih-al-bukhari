package com.bukhari;

/**
 * English translation sub-object nested inside each {@link Hadith}.
 *
 * <p>Mirrors the JSON shape:
 * <pre>{@code { "narrator": "...", "text": "..." }}</pre>
 */
public class HadithEnglish {

    private String narrator;
    private String text;

    /** English narrator name, e.g. "Umar bin Al-Khattab" */
    public String getNarrator() { return narrator != null ? narrator : ""; }

    /** Full English translation of the hadith */
    public String getText()     { return text     != null ? text     : ""; }

    @Override
    public String toString() {
        return "<HadithEnglish narrator=\"" + getNarrator() + "\">";
    }
}
