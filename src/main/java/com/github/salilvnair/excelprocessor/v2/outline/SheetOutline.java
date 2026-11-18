package com.github.salilvnair.excelprocessor.v2.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Headers, data row count and inferred column types of one sheet. */
public final class SheetOutline {
    private final String name;
    private final int headerRow;
    private final List<ColumnOutline> columns;
    private final long dataRows;
    private final long sampledRows;

    public SheetOutline(String name, int headerRow, List<ColumnOutline> columns, long dataRows, long sampledRows) {
        this.name = name;
        this.headerRow = headerRow;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        this.dataRows = dataRows;
        this.sampledRows = sampledRows;
    }

    public String name() {
        return name;
    }

    /** one based row the headers were found on, or -1 when the sheet is empty */
    public int headerRow() {
        return headerRow;
    }

    public List<ColumnOutline> columns() {
        return columns;
    }

    public List<String> headers() {
        List<String> headers = new ArrayList<>(columns.size());
        for (ColumnOutline column : columns) {
            headers.add(column.header());
        }
        return headers;
    }

    public Optional<ColumnOutline> column(String header) {
        return columns.stream().filter(c -> c.header().equals(header)).findFirst();
    }

    /** non-empty rows below the header row (all of them, not only the sampled ones) */
    public long dataRows() {
        return dataRows;
    }

    /** rows used for type inference */
    public long sampledRows() {
        return sampledRows;
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    @Override
    public String toString() {
        return name + columns + " rows=" + dataRows;
    }
}
