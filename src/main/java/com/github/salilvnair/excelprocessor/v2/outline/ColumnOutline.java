package com.github.salilvnair.excelprocessor.v2.outline;

/** One header column of a sheet: where it is, its inferred type and how often it has a value. */
public final class ColumnOutline {
    private final String header;
    private final int columnIndex;
    private final String column;
    private final ColumnType type;
    private final long nonBlankValues;
    private final String sample;

    public ColumnOutline(String header, int columnIndex, String column, ColumnType type, long nonBlankValues, String sample) {
        this.header = header;
        this.columnIndex = columnIndex;
        this.column = column;
        this.type = type;
        this.nonBlankValues = nonBlankValues;
        this.sample = sample;
    }

    public String header() {
        return header;
    }

    /** zero based */
    public int columnIndex() {
        return columnIndex;
    }

    /** column letters, e.g. {@code C} */
    public String column() {
        return column;
    }

    public ColumnType type() {
        return type;
    }

    /** non-blank values among the sampled rows */
    public long nonBlankValues() {
        return nonBlankValues;
    }

    /** first non-blank sampled value as text, or null */
    public String sample() {
        return sample;
    }

    @Override
    public String toString() {
        return column + ":" + header + "(" + type + ")";
    }
}
