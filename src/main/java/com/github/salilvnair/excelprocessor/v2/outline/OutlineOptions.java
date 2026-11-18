package com.github.salilvnair.excelprocessor.v2.outline;

/**
 * How {@code ExcelSheetReaderFactory.outline(...)} finds headers and how many rows it samples.
 * Immutable.
 */
public final class OutlineOptions {
    private final int headerRow;
    private final int sampleRows;
    private final String password;

    private OutlineOptions(int headerRow, int sampleRows, String password) {
        this.headerRow = headerRow;
        this.sampleRows = sampleRows;
        this.password = password;
    }

    /** First non-empty row is the header row; 1,000 rows are sampled for types. */
    public static OutlineOptions defaults() {
        return new OutlineOptions(-1, 1000, null);
    }

    /** One based header row; -1 (default) uses the first non-empty row. */
    public OutlineOptions headerRow(int headerRow) {
        return new OutlineOptions(headerRow, sampleRows, password);
    }

    /** Rows used for type inference; rows beyond it are only counted. */
    public OutlineOptions sampleRows(int sampleRows) {
        return new OutlineOptions(headerRow, Math.max(0, sampleRows), password);
    }

    /** Password of an encrypted XLSX/XLS file. */
    public OutlineOptions password(String password) {
        return new OutlineOptions(headerRow, sampleRows, password);
    }

    public int headerRow() {
        return headerRow;
    }

    public int sampleRows() {
        return sampleRows;
    }

    public String password() {
        return password;
    }
}
