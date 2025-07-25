package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.core.BaseExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.fixed.PatternValidator;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.ExcelInfo;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.Date;
import java.util.List;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetReader extends BaseExcelProcessor implements ExcelSheetReader {
    abstract void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context);
    abstract ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context);

    public static Object extractValueBasedOnCellType(Workbook workbook, Cell cell, CellInfo cellInfo) {
        Object cellValue = null;
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                cellValue = cell.getStringCellValue();
                cellInfo.setCellType(String.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                break;
            case NUMERIC:
                double numericCellValue = cell.getNumericCellValue();
                if(DateUtil.isCellDateFormatted(cell) && DateUtil.isValidExcelDate(numericCellValue)) {
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
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                cellInfo.setCellType(Boolean.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_BOOLEAN);
                break;
            case FORMULA:
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                CellValue formulaEvaluatedCellValue = null;
                try {
                    formulaEvaluatedCellValue = formulaEvaluator.evaluate(cell);
                }
                catch (Exception ex) {
                    switch (cell.getCachedFormulaResultType()) {
                        case NUMERIC:
                            numericCellValue = cell.getNumericCellValue();
                            if (DateUtil.isCellDateFormatted(cell) && DateUtil.isValidExcelDate(numericCellValue)) {
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
                        case STRING:
                            cellValue = cell.getStringCellValue().replaceAll("'", "");
                            cellInfo.setCellType(String.class);
                            cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                            break;
                    }
                    break;
                }
                switch (formulaEvaluatedCellValue.getCellType()) {
                    case NUMERIC:
                        numericCellValue = formulaEvaluatedCellValue.getNumberValue();
                        if(DateUtil.isCellDateFormatted(cell) && DateUtil.isValidExcelDate(numericCellValue)) {
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
                    case STRING:
                        cellValue = formulaEvaluatedCellValue.getStringValue().replaceAll("'", "");
                        cellInfo.setCellType(String.class);
                        cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                        break;
                }
                break;
        }
        return cellValue;
    }

    protected void extractCellPropertiesAndSetCellInfo(Workbook workbook, Cell cell, CellInfo cellInfo) {
        Color fgColor = cell.getCellStyle().getFillForegroundColorColor();
        Color bgColor = cell.getCellStyle().getFillBackgroundColorColor();
        cellInfo.setBackgroundColor(bgColor);
        cellInfo.setForegroundColor(fgColor);
        extractColorPropertiesAndSetCellInfo(fgColor, cellInfo, false);
        extractColorPropertiesAndSetCellInfo(bgColor, cellInfo, true);
    }

    private void extractColorPropertiesAndSetCellInfo(Color color, CellInfo cellInfo, boolean background) {
        if(color == null) {
            return;
        }
        String hex = null;
        short[] rgb =  null;
        if (color instanceof XSSFColor) {
            hex = ((XSSFColor) color).getARGBHex();
            hex = hex!=null && hex.length() > 2 ? hex.substring(2) : hex;
            byte[] rgbBytes = ((XSSFColor) color).getRGB();
            if(rgbBytes != null) {
                rgb =  new short[3];
                rgb[0]= rgbBytes[0] < 0 ? (short) (rgbBytes[0]+256) : rgbBytes[0];
                rgb[1]= rgbBytes[0] < 0 ? (short) (rgbBytes[1]+256) : rgbBytes[1];
                rgb[2]= rgbBytes[0] < 0 ? (short) (rgbBytes[2]+256) : rgbBytes[2];
            }
        }
        else if (color instanceof HSSFColor) {
            if (! (color.equals(HSSFColor.HSSFColorPredefined.AUTOMATIC.getColor()))) {
                hex = ((HSSFColor) color).getHexString();
            }
            rgb = ((HSSFColor) color).getTriplet();
        }
        if (background) {
            cellInfo.setBackgroundHexString(hex);
            cellInfo.setBackgroundRgb(rgb);
        }
        else {
            cellInfo.setForegroundHexString(hex);
            cellInfo.setForegroundRgb(rgb);
        }
    }

    protected boolean ignoreHeaderPatternMatchFound(String headerString, List<String> ignoreHeaderPatterns) {
        return ignoreHeaderPatterns.stream().anyMatch(patternString -> PatternValidator.find(patternString, headerString));
    }
}
