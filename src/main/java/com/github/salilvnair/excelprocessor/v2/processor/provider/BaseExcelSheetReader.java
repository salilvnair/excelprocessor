package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.*;

import java.util.Date;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetReader extends BaseExcelProcessor implements ExcelSheetReader {
    abstract void read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context);

    protected Object extractValueBasedOnCellType(Workbook workbook, Cell cell, CellInfo cellInfo) {
        Object cellValue = null;
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_STRING:
                cellValue = cell.getStringCellValue();
                cellInfo.setCellType(String.class);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                double numericCellValue = cell.getNumericCellValue();
                if(DateUtil.isCellDateFormatted(cell)) {
                    cellValue = cell.getDateCellValue();
                    cellInfo.setCellType(Date.class);
                }
                else {
                    cellValue = numericCellValue;
                    cellInfo.setCellType(Double.class);
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                cellInfo.setCellType(Boolean.class);
                break;
            case Cell.CELL_TYPE_FORMULA:
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cell);
                switch (cell.getCachedFormulaResultType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        numericCellValue = formulaEvaluatedCellValue.getNumberValue();
                        if(DateUtil.isValidExcelDate(numericCellValue)) {
                            cellValue = DateUtil.getJavaDate(numericCellValue);
                            cellInfo.setCellType(Date.class);
                        }
                        else {
                            cellValue = numericCellValue;
                            cellInfo.setCellType(Double.class);
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        cellValue = formulaEvaluatedCellValue.getStringValue().replaceAll("'", "");
                        cellInfo.setCellType(String.class);
                        break;
                }
                break;
        }
        return cellValue;
    }
}
