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
import java.util.Map;

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

    /** colour properties per (workbook, style index): every cell sharing a style has the same colours */
    private final Map<Object, Map<Short, CellInfo>> styleColorCache = new java.util.concurrent.ConcurrentHashMap<>();

    protected void extractCellPropertiesAndSetCellInfo(Workbook workbook, Cell cell, CellInfo cellInfo) {
        applyStyleColors(workbook, cell.getCellStyle(), cellInfo);
    }

    /** Copies the (cached) fill colours of {@code style} into {@code cellInfo}; {@code styleSource} scopes the cache. */
    protected void applyStyleColors(Object styleSource, CellStyle style, CellInfo cellInfo) {
        if(style == null) {
            return;
        }
        CellInfo colors = styleColorCache
                .computeIfAbsent(styleSource, w -> new java.util.concurrent.ConcurrentHashMap<>())
                .computeIfAbsent(style.getIndex(), index -> {
                    CellInfo c = new CellInfo();
                    Color fgColor = style.getFillForegroundColorColor();
                    Color bgColor = style.getFillBackgroundColorColor();
                    c.setBackgroundColor(bgColor);
                    c.setForegroundColor(fgColor);
                    extractColorPropertiesAndSetCellInfo(fgColor, c, false);
                    extractColorPropertiesAndSetCellInfo(bgColor, c, true);
                    return c;
                });
        cellInfo.setBackgroundColor(colors.getBackgroundColor());
        cellInfo.setForegroundColor(colors.getForegroundColor());
        cellInfo.setForegroundHexString(colors.getForegroundHexString());
        cellInfo.setBackgroundHexString(colors.getBackgroundHexString());
        cellInfo.setForegroundRgb(colors.getForegroundRgb() == null ? null : colors.getForegroundRgb().clone());
        cellInfo.setBackgroundRgb(colors.getBackgroundRgb() == null ? null : colors.getBackgroundRgb().clone());
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
                // bytes are signed in Java: convert each channel independently to 0..255
                rgb[0] = (short) (rgbBytes[0] & 0xFF);
                rgb[1] = (short) (rgbBytes[1] & 0xFF);
                rgb[2] = (short) (rgbBytes[2] & 0xFF);
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
