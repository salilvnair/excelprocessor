package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
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
}
