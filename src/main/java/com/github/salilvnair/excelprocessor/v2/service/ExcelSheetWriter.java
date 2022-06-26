package com.github.salilvnair.excelprocessor.v2.service;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public interface ExcelSheetWriter {
    default <T extends BaseSheet> void write(List<T> sheetData, ExcelSheetContext sheetContext) throws Exception {}

    default void write(Map<String, List<? extends BaseSheet>> sheets, ExcelSheetContext sheetContext) {}

    default <T extends BaseSheet> Workbook workbook(List<T> sheetData, ExcelSheetContext sheetContext) { return null;}

    default Workbook workbook(Map<String, List<? extends BaseSheet>> sheets, ExcelSheetContext sheetContext) {return null;}

    default void disposeStreamingWorkbook(Workbook workbook, ExcelSheetContext context) {
        if(context.writerContext().streamingWorkbook()) {
            SXSSFWorkbook sxssfWorkbook = (SXSSFWorkbook) workbook;
            sxssfWorkbook.dispose();
        }
    }

    static  int toIndentNumber(String name) {
        return ExcelSheetReader.toIndentNumber(name);
    }

    static String toIndentName(int number) {
        return ExcelSheetReader.toIndentName(number);
    }
}
