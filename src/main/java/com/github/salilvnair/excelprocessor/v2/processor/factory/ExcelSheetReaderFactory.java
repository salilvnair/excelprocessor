package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.outline.OutlineOptions;
import com.github.salilvnair.excelprocessor.v2.outline.WorkbookOutline;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.CsvSheetReaderImpl;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.WorkbookOutliner;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.ExcelSheetReaderImpl;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.StreamingExcelSheetReaderImpl;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;

import java.io.File;
import java.io.InputStream;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderFactory {
    private ExcelSheetReaderFactory(){}

    public static ExcelSheetReader generate() {
        return ExcelSheetReaderImpl.init();
    }
    public static ExcelSheetReader generate(boolean concurrent) {
        return ExcelSheetReaderImpl.init(concurrent, 100);
    }

    public static ExcelSheetReader generate(boolean concurrent, int batchSize) {
        return ExcelSheetReaderImpl.init(concurrent, batchSize);
    }

    /**
     * Opens a row-at-a-time XLSX reader over {@code file} (preferred: the file is not loaded into
     * memory). {@code context} supplies sheetName, ignoreHeaders/ignoreHeaderColumns and
     * headerFieldInfo; it may be {@code null}. Use in try-with-resources.
     */
    public static StreamingExcelSheetReader openStreaming(File file, ExcelSheetContext context) {
        return new StreamingExcelSheetReaderImpl(file, context);
    }

    public static StreamingExcelSheetReader openStreaming(File file) {
        return openStreaming(file, null);
    }

    /** Like {@link #openStreaming(File, ExcelSheetContext)}; the stream is buffered by POI, so prefer a File for very large inputs. */
    public static StreamingExcelSheetReader openStreaming(InputStream inputStream, ExcelSheetContext context) {
        return new StreamingExcelSheetReaderImpl(inputStream, context);
    }

    /**
     * Opens a row-at-a-time CSV/TSV reader with the same API (and the same {@code @Sheet} classes)
     * as {@link #openStreaming(File, ExcelSheetContext)}. The file is one sheet.
     */
    public static StreamingExcelSheetReader openCsv(File file, CsvFormat format, ExcelSheetContext context) {
        return new CsvSheetReaderImpl(file, format, context);
    }

    public static StreamingExcelSheetReader openCsv(File file) {
        return openCsv(file, CsvFormat.csv(), null);
    }

    /** CSV from a stream; it can be read once. */
    public static StreamingExcelSheetReader openCsv(InputStream inputStream, CsvFormat format, ExcelSheetContext context) {
        return new CsvSheetReaderImpl(inputStream, format, context);
    }

    /**
     * Lists the sheets of {@code file} (XLSX, XLS, CSV or TSV by extension) with their headers,
     * data row counts and inferred column types, without an annotated class. XLSX and CSV are
     * streamed, so this is quick and memory-flat on large files.
     */
    public static WorkbookOutline outline(File file) {
        return WorkbookOutliner.outline(file, OutlineOptions.defaults());
    }

    public static WorkbookOutline outline(File file, OutlineOptions options) {
        return WorkbookOutliner.outline(file, options);
    }

    public static WorkbookOutline outlineCsv(File file, CsvFormat format, OutlineOptions options) {
        return WorkbookOutliner.outlineCsv(file, format, options);
    }
}
