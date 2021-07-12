package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import org.apache.poi.ss.usermodel.*;

import java.util.Date;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetReader extends BaseExcelProcessor implements ExcelSheetReader {
    abstract void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context);
    abstract ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context);

    protected Object extractValueBasedOnCellType(Workbook workbook, Cell cell, CellInfo cellInfo) {
        Object cellValue = null;
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_STRING:
                cellValue = cell.getStringCellValue();
                cellInfo.setCellType(String.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                double numericCellValue = cell.getNumericCellValue();
                if(DateUtil.isCellDateFormatted(cell)) {
                    cellValue = cell.getDateCellValue();
                    cellInfo.setCellType(Date.class);
                    cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DATE);
                }
                else {
                    cellValue = numericCellValue;
                    cellInfo.setCellType(Double.class);
                    cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DOUBLE);
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                cellInfo.setCellType(Boolean.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_BOOLEAN);
                break;
            case Cell.CELL_TYPE_FORMULA:
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                CellValue formulaEvaluatedCellValue = null;
                try {
                    formulaEvaluatedCellValue = formulaEvaluator.evaluate(cell);
                }
                catch (Exception ex) {
                    switch (cell.getCachedFormulaResultType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            numericCellValue = cell.getNumericCellValue();
                            if (DateUtil.isValidExcelDate(numericCellValue)) {
                                cellValue = DateUtil.getJavaDate(numericCellValue);
                                cellInfo.setCellType(Date.class);
                                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DATE);
                            }
                            else {
                                cellValue = numericCellValue;
                                cellInfo.setCellType(Double.class);
                                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DOUBLE);
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            cellValue = cell.getStringCellValue().replaceAll("'", "");
                            cellInfo.setCellType(String.class);
                            cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                            break;
                    }
                    break;
                }
                switch (cell.getCachedFormulaResultType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        numericCellValue = formulaEvaluatedCellValue.getNumberValue();
                        if(DateUtil.isValidExcelDate(numericCellValue)) {
                            cellValue = DateUtil.getJavaDate(numericCellValue);
                            cellInfo.setCellType(Date.class);
                            cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DATE);
                        }
                        else {
                            cellValue = numericCellValue;
                            cellInfo.setCellType(Double.class);
                            cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DOUBLE);
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        cellValue = formulaEvaluatedCellValue.getStringValue().replaceAll("'", "");
                        cellInfo.setCellType(String.class);
                        cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                        break;
                }
                break;
        }
        return cellValue;
    }
}
