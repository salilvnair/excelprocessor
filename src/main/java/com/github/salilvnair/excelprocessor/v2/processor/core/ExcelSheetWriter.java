package com.github.salilvnair.excelprocessor.v2.processor.core;

import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public interface ExcelSheetWriter {
    default <T extends BaseSheet> void write(List<T> sheet, String filePath) {}

    default void write(Map<String, List<? extends BaseSheet>> sheet, String filePath) {}

    default <T extends BaseSheet> Workbook workbook(List<T> sheet) { return null;}

    default Workbook workbook(Map<String, List<? extends BaseSheet>> sheet) {return null;}
}
