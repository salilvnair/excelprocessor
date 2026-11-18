package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;

import java.util.List;

/**
 * Forward-only source of raw rows (XLSX sheet XML, CSV text). The streaming readers map rows from
 * any source the same way, so headers, keys and conversions do not depend on the file format.
 */
interface RowSource extends AutoCloseable {

    /** Moves to the next row that has a {@code <row>}/line; false at the end of the sheet. */
    boolean nextRow();

    /** Zero based index of the current row. */
    int rowIndex();

    /** Cells of the current row in column order; missing cells are simply absent. */
    List<RawCell> cells();

    /** Java value of a cell; also records its type on {@code cellInfo}. */
    Object value(RawCell cell, CellInfo cellInfo);

    /** False when every cell is text (CSV), so types have to be inferred from the text. */
    default boolean typedCells() {
        return true;
    }

    /** Adds format specific details (fill colours) to {@code cellInfo}. */
    default void decorate(RawCell cell, CellInfo cellInfo) {}

    /** Releases the underlying stream. Idempotent, never throws. */
    @Override
    void close();
}
