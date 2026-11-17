package com.github.salilvnair.excelprocessor.v2.processor.helper.style;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.Arrays;

/**
 * Snapshot of every visual property of a {@link CellStyle}, used to deduplicate styles produced
 * by user code (for example custom style tasks) where no {@link CellStyleSpec} is available.
 *
 * @author Salil V Nair
 */
final class StyleSignature {
    private final Object[] values;
    private final int hash;

    private StyleSignature(Object[] values) {
        this.values = values;
        this.hash = Arrays.hashCode(values);
    }

    static StyleSignature of(CellStyle s) {
        return new StyleSignature(new Object[]{
                s.getDataFormatString(), s.getFontIndex(), s.getHidden(), s.getLocked(), s.getQuotePrefixed(),
                s.getAlignment(), s.getWrapText(), s.getVerticalAlignment(), s.getRotation(), s.getIndention(),
                s.getBorderLeft(), s.getBorderRight(), s.getBorderTop(), s.getBorderBottom(),
                s.getLeftBorderColor(), s.getRightBorderColor(), s.getTopBorderColor(), s.getBottomBorderColor(),
                s.getFillPattern(), s.getFillBackgroundColor(), s.getFillForegroundColor(),
                colorKey(s.getFillForegroundColorColor()), colorKey(s.getFillBackgroundColorColor()),
                s.getShrinkToFit()
        });
    }

    private static Object colorKey(Color color) {
        if (color instanceof XSSFColor) {
            return ((XSSFColor) color).getARGBHex();
        }
        if (color instanceof HSSFColor) {
            return ((HSSFColor) color).getHexString();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof StyleSignature && Arrays.equals(values, ((StyleSignature) o).values));
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
