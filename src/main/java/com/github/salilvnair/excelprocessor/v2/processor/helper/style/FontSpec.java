package com.github.salilvnair.excelprocessor.v2.processor.helper.style;

import org.apache.poi.ss.usermodel.Font;

import java.util.Objects;

/**
 * Immutable, value-based description of a font. Two equal specs always resolve to the same
 * workbook {@link Font}, so repeated equivalent definitions do not grow the font table.
 *
 * @author Salil V Nair
 */
public final class FontSpec {
    private final boolean bold;
    private final boolean italic;
    private final boolean strikeout;
    private final short color;
    private final byte underline;
    private final String fontName;
    private final short fontHeight;

    private FontSpec(Builder b) {
        this.bold = b.bold;
        this.italic = b.italic;
        this.strikeout = b.strikeout;
        this.color = b.color;
        this.underline = b.underline;
        this.fontName = b.fontName == null || b.fontName.isEmpty() ? null : b.fontName;
        this.fontHeight = b.fontHeight;
    }

    public static Builder builder() {
        return new Builder();
    }

    void applyTo(Font font) {
        font.setBold(bold);
        font.setItalic(italic);
        font.setStrikeout(strikeout);
        font.setColor(color);
        if (underline != Font.U_NONE) {
            font.setUnderline(underline);
        }
        if (fontName != null) {
            font.setFontName(fontName);
        }
        if (fontHeight > -1) {
            font.setFontHeight(fontHeight);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FontSpec)) return false;
        FontSpec that = (FontSpec) o;
        return bold == that.bold && italic == that.italic && strikeout == that.strikeout
                && color == that.color && underline == that.underline && fontHeight == that.fontHeight
                && Objects.equals(fontName, that.fontName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold, italic, strikeout, color, underline, fontName, fontHeight);
    }

    @Override
    public String toString() {
        return "FontSpec{bold=" + bold + ", italic=" + italic + ", strikeout=" + strikeout + ", color=" + color
                + ", underline=" + underline + ", fontName=" + fontName + ", fontHeight=" + fontHeight + '}';
    }

    public static final class Builder {
        private boolean bold;
        private boolean italic;
        private boolean strikeout;
        private short color = Font.COLOR_NORMAL;
        private byte underline = Font.U_NONE;
        private String fontName;
        private short fontHeight = -1;

        public Builder bold(boolean v) { this.bold = v; return this; }
        public Builder italic(boolean v) { this.italic = v; return this; }
        public Builder strikeout(boolean v) { this.strikeout = v; return this; }
        public Builder color(short v) { this.color = v; return this; }
        public Builder underline(byte v) { this.underline = v; return this; }
        public Builder fontName(String v) { this.fontName = v; return this; }
        public Builder fontHeight(short v) { this.fontHeight = v; return this; }
        public FontSpec build() { return new FontSpec(this); }
    }
}
