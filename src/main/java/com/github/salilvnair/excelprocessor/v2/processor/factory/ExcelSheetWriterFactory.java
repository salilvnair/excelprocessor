package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.ExcelSheetWriterImpl;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.StreamingExcelSheetWriterImpl;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriterFactory {
    private ExcelSheetWriterFactory(){}

    public static ExcelSheetWriter generate() {
        return ExcelSheetWriterImpl.init();
    }

    /**
     * Opens a row-at-a-time XLSX writer. Always use it in try-with-resources so SXSSF temp files are
     * released on success and on failure.
     */
    public static StreamingExcelSheetWriter openStreaming(ExcelSheetContext sheetContext) {
        return new StreamingExcelSheetWriterImpl(sheetContext);
    }

    public static StreamingExcelSheetWriter openStreaming() {
        return openStreaming(ExcelSheetContext.builder().build());
    }
}
