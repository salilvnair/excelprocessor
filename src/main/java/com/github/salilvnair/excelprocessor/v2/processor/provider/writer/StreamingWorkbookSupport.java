package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Shared SXSSF setup for the streaming writers.
 *
 * @author Salil V Nair
 */
final class StreamingWorkbookSupport {
    static final int DEFAULT_ROW_WINDOW_SIZE = 100;

    private StreamingWorkbookSupport() {}

    /** Result of {@link #open}: the workbook and whether this call created it (and so must dispose it on failure). */
    static final class Opened {
        final SXSSFWorkbook workbook;
        final boolean created;

        Opened(SXSSFWorkbook workbook, boolean created) {
            this.workbook = workbook;
            this.created = created;
        }
    }

    /**
     * Returns the streaming workbook to write {@code sheet} into: the workbook of a previous sheet of
     * the same write call, a streaming wrapper around the template, or a new workbook.
     */
    static Opened open(Sheet sheet, ExcelSheetWriterContext context) {
        int windowSize = windowSize(sheet);
        Workbook existing = context.containsExistingWorkbook() ? context.existingWorkbook() : null;
        if (existing instanceof SXSSFWorkbook) {
            return new Opened((SXSSFWorkbook) existing, false);
        }
        XSSFWorkbook base;
        if (existing != null) {
            base = asXssf(existing, "existing workbook");
        }
        else if (context.template() != null) {
            base = asXssf(context.template(), "template");
        }
        else {
            base = new XSSFWorkbook();
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook(base, windowSize);
        workbook.setCompressTempFiles(true);
        return new Opened(workbook, true);
    }

    static SXSSFSheet sheet(SXSSFWorkbook workbook, Sheet sheet) {
        SXSSFSheet sxssfSheet = workbook.getSheet(sheet.value());
        if (sxssfSheet == null) {
            sxssfSheet = workbook.createSheet(sheet.value());
        }
        sxssfSheet.setRandomAccessWindowSize(windowSize(sheet));
        SheetFeatures.prepareStreamingSheet(sxssfSheet, sheet);
        return sxssfSheet;
    }

    static int windowSize(Sheet sheet) {
        return sheet.streamingRowWindowSize() > 0 ? sheet.streamingRowWindowSize() : DEFAULT_ROW_WINDOW_SIZE;
    }

    private static XSSFWorkbook asXssf(Workbook workbook, String what) {
        if (workbook instanceof XSSFWorkbook) {
            return (XSSFWorkbook) workbook;
        }
        throw new ExcelSheetWriterException("Streaming (SXSSF) requires an XLSX " + what + " but got "
                + workbook.getClass().getSimpleName() + ". Use streamingWorkbook=false for XLS output.");
    }
}
