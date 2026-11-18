package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

/** One cell as a {@link RowSource} delivers it, before conversion to a Java value. */
final class RawCell {
    /** zero based column index */
    int column;
    /** XLSX cell type attribute ({@code s}, {@code str}, {@code inlineStr}, {@code b}, {@code e}, or null for numbers) */
    String type;
    /** XLSX style index, 0 when none */
    int style;
    /** raw text: the shared-string index, number text, inline text or CSV field */
    String value;
    boolean inlineText;
    /** already converted value, for sources backed by the POI usermodel */
    Object parsed;

    static RawCell text(int column, String value) {
        RawCell cell = new RawCell();
        cell.column = column;
        cell.type = "inlineStr";
        cell.value = value;
        cell.inlineText = true;
        return cell;
    }
}
