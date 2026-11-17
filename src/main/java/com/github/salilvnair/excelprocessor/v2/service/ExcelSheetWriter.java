package com.github.salilvnair.excelprocessor.v2.service;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * List based writer API.
 * <p>
 * <b>Ownership:</b> the {@code write(...)} methods own the workbook they build: they serialize it and
 * always release streaming (SXSSF) temp files, on success and on failure. The {@code workbook(...)}
 * methods hand the workbook to the caller, who must then call {@link #dispose(Workbook)} (in a
 * {@code finally} block) once it has been written. For very large exports prefer
 * {@code ExcelSheetWriterFactory.openStreaming(...)}, which writes rows one at a time.
 *
 * @author Salil V Nair
 */
public interface ExcelSheetWriter {
    default <T extends BaseSheet> void write(List<T> sheetData, ExcelSheetContext sheetContext) throws Exception {}

    default <T extends BaseSheet> void write(ExcelSheetContext sheetContext, List<List<? extends BaseSheet>> multiOrientedSheets) throws Exception {}

    default void write(Map<String, List<? extends BaseSheet>> sheets, ExcelSheetContext sheetContext) {}

    /** Writes {@code sheetData} to {@code outputStream} (not closed) without materializing a byte array. */
    default <T extends BaseSheet> void write(List<T> sheetData, ExcelSheetContext sheetContext, OutputStream outputStream) throws Exception {}

    /** Writes every sheet of {@code sheets} to {@code outputStream} (not closed). */
    default void write(Map<String, List<? extends BaseSheet>> sheets, ExcelSheetContext sheetContext, OutputStream outputStream) throws Exception {}

    default <T extends BaseSheet> Workbook workbook(List<T> sheetData, ExcelSheetContext sheetContext) { return null;}

    default <T extends BaseSheet> Workbook workbook(ExcelSheetContext sheetContext, List<List<? extends BaseSheet>> multiOrientedSheets) throws Exception { return null;}

    default Workbook workbook(Map<String, List<? extends BaseSheet>> sheets, ExcelSheetContext sheetContext) {return null;}

    default Workbook workbook(ExcelSheetContext sheetContext, Map<String, List<List<? extends BaseSheet>>> multiOrientedSheets) throws Exception { return null;}

    /** @deprecated use {@link #dispose(Workbook)}; kept for source compatibility */
    @Deprecated
    default void disposeStreamingWorkbook(Workbook workbook, ExcelSheetContext context) {
        dispose(workbook);
    }

    /**
     * Releases the temp files of a streaming (SXSSF) workbook. Safe to call more than once and a
     * no-op for any other workbook type.
     */
    static void dispose(Workbook workbook) {
        if(workbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) workbook).dispose();
        }
    }

    static  int toIndentNumber(String name) {
        return ExcelSheetReader.toIndentNumber(name);
    }

    static String toIndentName(int number) {
        return ExcelSheetReader.toIndentName(number);
    }
}
