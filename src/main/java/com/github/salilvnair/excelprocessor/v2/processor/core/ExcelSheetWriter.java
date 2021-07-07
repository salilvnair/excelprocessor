package com.github.salilvnair.excelprocessor.v2.processor.core;

import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public interface ExcelSheetWriter {
    default <T extends BaseExcelSheet> void write(List<T> sheet, String filePath) {}

    default void write(Map<String, List<? extends BaseExcelSheet>> sheet, String filePath) {}

    default <T extends BaseExcelSheet> Workbook workbook(List<T> sheet) { return null;}

    default Workbook workbook(Map<String, List<? extends BaseExcelSheet>> sheet) {return null;}
}
