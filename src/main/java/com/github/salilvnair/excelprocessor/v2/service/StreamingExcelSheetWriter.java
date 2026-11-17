package com.github.salilvnair.excelprocessor.v2.service;

import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Row-at-a-time XLSX writer backed by SXSSF. Unlike the list based {@link ExcelSheetWriter}, rows
 * are written as they are produced and never retained, so heap use is bounded by the SXSSF row
 * window (see {@code @Sheet(streamingRowWindowSize)}) regardless of the number of rows.
 *
 * <pre>{@code
 * try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(context)) {
 *     writer.beginSheet(ReportSheet.class, orderedHeaders);   // or beginSheet(FixedSheet.class)
 *     ReportSheet row;
 *     while ((row = source.next()) != null) {
 *         writer.writeRow(row);
 *     }
 *     writer.finish(outputStream);                             // or finish(path)
 * }                                                            // temp files always released
 * }</pre>
 *
 * Supported: horizontal sheets with fixed {@code @Cell} headers or dynamic headers, data/header
 * cell styles (shared through the workbook style cache), style tasks, hyperlinks and pictures.
 * Not supported: vertical, sectional, merged-header and user-defined-template sheets.
 * Instances are not thread-safe.
 */
public interface StreamingExcelSheetWriter extends AutoCloseable {

    /** Starts a new sheet whose headers are the {@code @Cell} fields of {@code sheetClass}. */
    <T extends BaseSheet> StreamingExcelSheetWriter beginSheet(Class<T> sheetClass);

    /**
     * Starts a new dynamic-header sheet. {@code orderedHeaders} (unique, in column order) is frozen
     * for the whole sheet; a row lacking a header gets an empty cell.
     */
    <T extends DynamicHeaderSheet> StreamingExcelSheetWriter beginSheet(Class<T> sheetClass, List<String> orderedHeaders);

    /** Writes one row to the current sheet. The row object is not retained. */
    void writeRow(BaseSheet row);

    /** Writes every row of {@code source} until it returns {@code null}. */
    default <T extends BaseSheet> void writeRows(RowSource<T> source) throws Exception {
        T row;
        while ((row = source.next()) != null) {
            writeRow(row);
        }
    }

    /** Rows written to the current sheet. */
    long rowsWritten();

    /** Rows written to all sheets. */
    long totalRowsWritten();

    /** Serializes the workbook to {@code outputStream} (which is not closed). Can be called once. */
    void finish(OutputStream outputStream) throws IOException;

    /** Serializes the workbook to {@code path}. Can be called once. */
    void finish(Path path) throws IOException;

    /** The underlying streaming workbook, for advanced use (do not write rows to it directly). */
    Workbook workbook();

    /** Releases SXSSF temp files and closes the workbook. Idempotent, never throws. */
    @Override
    void close();

    /** A pull-based row source; {@code null} signals the end of input. */
    @FunctionalInterface
    interface RowSource<T> {
        T next() throws Exception;
    }
}
