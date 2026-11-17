package com.github.salilvnair.excelprocessor.v2.processor.helper.style;

import org.apache.poi.ss.usermodel.*;

import java.util.Objects;

/**
 * Immutable, value-based description of the <em>effective</em> style of a cell: an optional base
 * style (an existing style of the destination workbook, or a style of a separate style-template
 * workbook) plus every property the library sets on top of it.
 * <p>
 * Equal specs resolve to one shared workbook {@link CellStyle} through {@link CellStyleRegistry},
 * so the number of workbook styles is proportional to the number of distinct styles, not to the
 * number of cells written. A {@code null} property means "leave the base value unchanged".
 *
 * @author Salil V Nair
 */
public final class CellStyleSpec {
    /** index of a style of the destination workbook to start from, or -1 for a blank style */
    private final int baseStyleIndex;
    /** style of a <em>different</em> workbook (style template) to clone from; compared by identity */
    private final CellStyle templateStyle;
    private final Boolean wrapText;
    private final Boolean hidden;
    private final BorderStyle border;
    private final Short borderColor;
    private final FillPatternType fillPattern;
    private final Short fillForegroundColor;
    private final Short fillBackgroundColor;
    private final FontSpec font;
    private final String dataFormat;

    private CellStyleSpec(Builder b) {
        this.baseStyleIndex = b.baseStyleIndex;
        this.templateStyle = b.templateStyle;
        this.wrapText = b.wrapText;
        this.hidden = b.hidden;
        this.border = b.border;
        this.borderColor = b.borderColor;
        this.fillPattern = b.fillPattern;
        this.fillForegroundColor = b.fillForegroundColor;
        this.fillBackgroundColor = b.fillBackgroundColor;
        this.font = b.font;
        this.dataFormat = b.dataFormat;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Applies this spec to a freshly created style of {@code workbook}. */
    void applyTo(CellStyle style, Workbook workbook, CellStyleRegistry registry) {
        if (templateStyle != null) {
            style.cloneStyleFrom(templateStyle);
            style.setWrapText(templateStyle.getWrapText());
        }
        else if (baseStyleIndex > -1) {
            style.cloneStyleFrom(workbook.getCellStyleAt(baseStyleIndex));
        }
        if (wrapText != null) {
            style.setWrapText(wrapText);
        }
        if (hidden != null) {
            style.setHidden(hidden);
        }
        if (border != null) {
            style.setBorderTop(border);
            style.setBorderRight(border);
            style.setBorderBottom(border);
            style.setBorderLeft(border);
        }
        if (borderColor != null) {
            style.setTopBorderColor(borderColor);
            style.setRightBorderColor(borderColor);
            style.setBottomBorderColor(borderColor);
            style.setLeftBorderColor(borderColor);
        }
        if (fillPattern != null) {
            style.setFillPattern(fillPattern);
        }
        if (fillForegroundColor != null) {
            style.setFillForegroundColor(fillForegroundColor);
        }
        if (fillBackgroundColor != null) {
            style.setFillBackgroundColor(fillBackgroundColor);
        }
        if (font != null) {
            style.setFont(registry.font(workbook, font));
        }
        if (dataFormat != null) {
            style.setDataFormat(workbook.createDataFormat().getFormat(dataFormat));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellStyleSpec)) return false;
        CellStyleSpec that = (CellStyleSpec) o;
        return baseStyleIndex == that.baseStyleIndex
                && templateStyle == that.templateStyle
                && Objects.equals(wrapText, that.wrapText)
                && Objects.equals(hidden, that.hidden)
                && border == that.border
                && Objects.equals(borderColor, that.borderColor)
                && fillPattern == that.fillPattern
                && Objects.equals(fillForegroundColor, that.fillForegroundColor)
                && Objects.equals(fillBackgroundColor, that.fillBackgroundColor)
                && Objects.equals(font, that.font)
                && Objects.equals(dataFormat, that.dataFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseStyleIndex, System.identityHashCode(templateStyle), wrapText, hidden, border,
                borderColor, fillPattern, fillForegroundColor, fillBackgroundColor, font, dataFormat);
    }

    @Override
    public String toString() {
        return "CellStyleSpec{base=" + baseStyleIndex + ", template=" + (templateStyle != null ? templateStyle.getIndex() : null)
                + ", wrapText=" + wrapText + ", hidden=" + hidden + ", border=" + border + ", borderColor=" + borderColor
                + ", fillPattern=" + fillPattern + ", fillFg=" + fillForegroundColor + ", fillBg=" + fillBackgroundColor
                + ", font=" + font + ", dataFormat=" + dataFormat + '}';
    }

    public static final class Builder {
        private int baseStyleIndex = -1;
        private CellStyle templateStyle;
        private Boolean wrapText;
        private Boolean hidden;
        private BorderStyle border;
        private Short borderColor;
        private FillPatternType fillPattern;
        private Short fillForegroundColor;
        private Short fillBackgroundColor;
        private FontSpec font;
        private String dataFormat;

        public Builder baseStyleIndex(int v) { this.baseStyleIndex = v; return this; }
        public Builder templateStyle(CellStyle v) { this.templateStyle = v; return this; }
        public Builder wrapText(boolean v) { this.wrapText = v; return this; }
        public Builder hidden(boolean v) { this.hidden = v; return this; }
        public Builder border(BorderStyle v) { this.border = v; return this; }
        public Builder borderColor(short v) { this.borderColor = v; return this; }
        public Builder fillPattern(FillPatternType v) { this.fillPattern = v; return this; }
        public Builder fillForegroundColor(short v) { this.fillForegroundColor = v; return this; }
        public Builder fillBackgroundColor(short v) { this.fillBackgroundColor = v; return this; }
        public Builder font(FontSpec v) { this.font = v; return this; }
        public Builder dataFormat(String v) { this.dataFormat = v; return this; }
        public CellStyleSpec build() { return new CellStyleSpec(this); }
    }
}
