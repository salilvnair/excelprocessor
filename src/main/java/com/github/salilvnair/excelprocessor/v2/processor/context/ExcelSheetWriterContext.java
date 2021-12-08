package com.github.salilvnair.excelprocessor.v2.processor.context;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriterContext extends BaseExcelSheetContext {
    public Workbook workbook() {
        return super.getWorkbook();
    }
}
