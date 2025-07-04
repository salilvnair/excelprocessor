package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.*;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.NumberStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.StyleTemplateCellInfo;
import com.github.salilvnair.excelprocessor.v2.model.TextStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.task.helper.ExcelCellStyleTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.DateFormatPattern;
import com.github.salilvnair.excelprocessor.v2.type.NumberCategoryFormat;
import com.github.salilvnair.excelprocessor.v2.type.NumberPrecisionFormat;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.Map;

public class DataCellStyleWriterUtil {
    private DataCellStyleWriterUtil(){}


    public static void applyCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        writerContext.setRowCell(rowCell);
        writerContext.setCellField(cellField);
        writerContext.setCellValue(fieldValue);
        if(dataCellStyle.conditional()) {
            applyConditionalCellStyle(sheet, cell, rowCell, cellField, writerContext);
        }
        else if(!StringUtils.isEmpty(dataCellStyle.customTask()) || dataCellStyle.customTasks().length > 0) {
            executeCustomTaskWithCellStyle(sheet, cell, rowCell, cellField, writerContext);
        }
        else {
            applyCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
    }

    public static void applyDynamicCellStyles(Sheet sheet, String header, org.apache.poi.ss.usermodel.Cell rowCell, Object fieldValue, ExcelSheetWriterContext writerContext) {
        Map<String, DataCellStyleInfo> dynamicHeaderDataCellStyleInfo = writerContext.getDynamicHeaderDataCellStyleInfo();
        DataCellStyleInfo dataCellStyleInfo = null;
        if(dynamicHeaderDataCellStyleInfo == null || !dynamicHeaderDataCellStyleInfo.containsKey(header)) {
            BaseSheet sheetDataObj = writerContext.getSheetDataObj();
            if(sheetDataObj != null) {
                DataCellStyle dataCellStyle  = sheetDataObj.getClass().getAnnotation(DataCellStyle.class);
                dataCellStyleInfo = extractDataCellStyleInfo(dataCellStyle);
            }
            else {
                return;
            }
        }
        else {
            dataCellStyleInfo = dynamicHeaderDataCellStyleInfo.get(header);
        }
        if(dataCellStyleInfo == null) {
            return;
        }
        writerContext.setRowCell(rowCell);
        writerContext.setCellValue(fieldValue);
        if(dataCellStyleInfo.isConditional()) {
            applyConditionalDynamicCellStyle(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
        else if(!StringUtils.isEmpty(dataCellStyleInfo.getCustomTask()) || (dataCellStyleInfo.getCustomTasks() != null && dataCellStyleInfo.getCustomTasks().length > 0)) {
            executeCustomTaskWithDynamicCellStyle(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
        else {
            applyDynamicCellStyles(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
    }

    private static void executeCustomTaskWithCellStyle(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        if(dataCellStyle.applyDefaultStyles()) {
            applyCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
        executeCustomTask(dataCellStyle, sheet, cell, rowCell, cellField, writerContext);
    }

    private static void executeCustomTaskWithDynamicCellStyle(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        writerContext.setHeader(header);
        if(dataCellStyleInfo.isApplyDefaultStyles()) {
            applyDynamicCellStyles(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
        executeCustomTask(dataCellStyleInfo, sheet, rowCell, writerContext);
    }

    private static void executeCustomTask(DataCellStyle dataCellStyle, Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        if(!StringUtils.isEmpty(dataCellStyle.customTask())) {
            ExcelCellStyleTaskExecutor.execute(dataCellStyle.customTask(), sheet, writerContext);
        }
        else {
            for (String customTask : dataCellStyle.customTasks()) {
                ExcelCellStyleTaskExecutor.execute(customTask, sheet, writerContext);
            }
        }
    }

    private static void executeCustomTask(DataCellStyleInfo dataCellStyle, Sheet sheet, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(!StringUtils.isEmpty(dataCellStyle.getCustomTask())) {
            ExcelCellStyleTaskExecutor.execute(dataCellStyle.getCustomTask(), sheet, writerContext);
        }
        else {
            for (String customTask : dataCellStyle.getCustomTasks()) {
                ExcelCellStyleTaskExecutor.execute(customTask, sheet, writerContext);
            }
        }
    }

    private static void applyConditionalCellStyle(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle =  extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        Object object = ExcelCellStyleTaskExecutor.execute(dataCellStyle.condition(), sheet, writerContext);
        if(ObjectUtil.nonNullOrBooleanTrue(object)) {
            applyCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
    }

    private static void applyConditionalDynamicCellStyle(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        writerContext.setHeader(header);
        Object object = ExcelCellStyleTaskExecutor.execute(dataCellStyleInfo.getCondition(), sheet, writerContext);
        if(ObjectUtil.nonNullOrBooleanTrue(object)) {
            applyDynamicCellStyles(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
    }

    public static void applyCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle =  extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null && !dataCellStyle.ignoreStyleTemplate()) {
            if(writerContext.defaultDataCellStyle() != null ) {
                cellStyle = writerContext.defaultDataCellStyle();
            }
            else {
                cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
                writerContext.setDefaultDataCellStyle(cellStyle);
            }
            StyleTemplateCell styleTemplateCell = dataCellStyle.styleTemplateCell();
            int rowIndex = styleTemplateCell.row()  - 1;
            int columnIndex = ExcelSheetReader.toIndentNumber(styleTemplateCell.column())  - 1;
            if(writerContext.styleTemplate().getSheet(sheet.value()) != null) {
                CellStyle templateCellStyle = writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getCellStyle();
                cellStyle.cloneStyleFrom(templateCellStyle);
                // Copy cell width
                rowCell.getSheet().setColumnWidth(rowCell.getColumnIndex(), writerContext.styleTemplate().getSheet(sheet.value()).getColumnWidth(writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getColumnIndex()));
                // Copy text wrapping
                cellStyle.setWrapText(templateCellStyle.getWrapText());
            }
        }
        else {
            if(writerContext.defaultDataCellStyle() != null ) {
                cellStyle = writerContext.defaultDataCellStyle();
            }
            else {
                cellStyle = rowCell.getCellStyle();
                if (cellStyle == null || cellStyle.getDataFormat() == 0) {
                    cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
                }
                writerContext.setDefaultDataCellStyle(cellStyle);
            }
        }

        cellStyle.setWrapText(dataCellStyle.wrapText());
        if(dataCellStyle.hasBorderStyle()) {
            cellStyle.setBorderTop(dataCellStyle.borderStyle());
            cellStyle.setBorderRight(dataCellStyle.borderStyle());
            cellStyle.setBorderBottom(dataCellStyle.borderStyle());
            cellStyle.setBorderLeft(dataCellStyle.borderStyle());
        }
        if(dataCellStyle.hasBorderColor()) {
            cellStyle.setTopBorderColor(dataCellStyle.borderColor().getIndex());
            cellStyle.setRightBorderColor(dataCellStyle.borderColor().getIndex());
            cellStyle.setBottomBorderColor(dataCellStyle.borderColor().getIndex());
            cellStyle.setLeftBorderColor(dataCellStyle.borderColor().getIndex());
        }

        if(dataCellStyle.hasBackgroundColor()) {
            cellStyle.setFillPattern(dataCellStyle.fillPattern());
            cellStyle.setFillForegroundColor(dataCellStyle.backgroundColor().getIndex());
            cellStyle.setFillBackgroundColor(dataCellStyle.backgroundColor().getIndex());
        }

        if(dataCellStyle.hasForegroundColor()) {
            cellStyle.setFillPattern(dataCellStyle.fillPattern());
            cellStyle.setFillForegroundColor(dataCellStyle.foregroundColor().getIndex());
        }
        if(dataCellStyle.customTextStyle()) {
            applyTextStyleIfApplicable(rowCell, cellStyle, extractTextStyleInfo(dataCellStyle.textStyle()), writerContext);
        }
        applyNumberStyleIfApplicable(rowCell, cellStyle, extractNumberStyleInfo(dataCellStyle.numberStyle()), writerContext);
        rowCell.setCellStyle(cellStyle);
    }

    public static void applyDynamicCellStyles(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null && !dataCellStyleInfo.isIgnoreStyleTemplate()) {
            if(writerContext.defaultDataCellStyle() != null ) {
                cellStyle = writerContext.defaultDataCellStyle();
            }
            else {
                cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
                writerContext.setDefaultDataCellStyle(cellStyle);
            }
            StyleTemplateCellInfo styleTemplateCellInfo = dataCellStyleInfo.getStyleTemplateCellInfo();
            int rowIndex = styleTemplateCellInfo.getRow()  - 1;
            int columnIndex = ExcelSheetReader.toIndentNumber(styleTemplateCellInfo.getColumn())  - 1;
            if(writerContext.styleTemplate().getSheet(sheet.value()) != null) {
                CellStyle templateCellStyle = writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getCellStyle();
                cellStyle.cloneStyleFrom(templateCellStyle);
                // Copy cell width
                rowCell.getSheet().setColumnWidth(rowCell.getColumnIndex(), writerContext.styleTemplate().getSheet(sheet.value()).getColumnWidth(writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getColumnIndex()));
                // Copy text wrapping
                cellStyle.setWrapText(templateCellStyle.getWrapText());
            }
        }
        else {
            if(writerContext.defaultDataCellStyle() != null ) {
                cellStyle = writerContext.defaultDataCellStyle();
            }
            else {
                cellStyle = rowCell.getCellStyle();
                if (cellStyle == null || cellStyle.getDataFormat() == 0) {
                    cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
                }
            }
        }

        cellStyle.setWrapText(dataCellStyleInfo.isWrapText());
        if(dataCellStyleInfo.isHasBorderStyle()) {
            cellStyle.setBorderTop(dataCellStyleInfo.getBorderStyle());
            cellStyle.setBorderRight(dataCellStyleInfo.getBorderStyle());
            cellStyle.setBorderBottom(dataCellStyleInfo.getBorderStyle());
            cellStyle.setBorderLeft(dataCellStyleInfo.getBorderStyle());
        }
        if(dataCellStyleInfo.isHasBorderColor()) {
            cellStyle.setTopBorderColor(dataCellStyleInfo.getBorderColor().getIndex());
            cellStyle.setRightBorderColor(dataCellStyleInfo.getBorderColor().getIndex());
            cellStyle.setBottomBorderColor(dataCellStyleInfo.getBorderColor().getIndex());
            cellStyle.setLeftBorderColor(dataCellStyleInfo.getBorderColor().getIndex());
        }

        if(dataCellStyleInfo.isHasBackgroundColor()) {
            cellStyle.setFillPattern(dataCellStyleInfo.getFillPattern());
            cellStyle.setFillForegroundColor(dataCellStyleInfo.getBackgroundColor().getIndex());
            cellStyle.setFillBackgroundColor(dataCellStyleInfo.getBackgroundColor().getIndex());
        }

        if(dataCellStyleInfo.isHasForegroundColor()) {
            cellStyle.setFillPattern(dataCellStyleInfo.getFillPattern());
            cellStyle.setFillForegroundColor(dataCellStyleInfo.getForegroundColor().getIndex());
        }

        if(dataCellStyleInfo.isCustomTextStyle() && dataCellStyleInfo.getTextStyleInfo()!=null) {
            applyTextStyleIfApplicable(rowCell, cellStyle, dataCellStyleInfo.getTextStyleInfo(), writerContext);
        }
        applyNumberStyleIfApplicable(rowCell, cellStyle, dataCellStyleInfo.getNumberStyleInfo(), writerContext);
        rowCell.setCellStyle(cellStyle);
    }

    private static void applyTextStyleIfApplicable(org.apache.poi.ss.usermodel.Cell rowCell, CellStyle cellStyle, TextStyleInfo textStyleInfo, ExcelSheetWriterContext writerContext) {
        Font font = rowCell.getSheet().getWorkbook().createFont();
        font.setBold(textStyleInfo.isBold());
        font.setItalic(textStyleInfo.isItalic());
        font.setColor(textStyleInfo.getColor().getIndex());
        font.setStrikeout(textStyleInfo.isStrikeout());
        if(StringUtils.isNotEmpty(textStyleInfo.getFontName())) {
            font.setFontName(textStyleInfo.getFontName());
        }
        if(textStyleInfo.getFontHeight() > -1) {
            font.setFontHeight(textStyleInfo.getFontHeight());
        }
        cellStyle.setFont(font);
    }

    private static void applyNumberStyleIfApplicable(org.apache.poi.ss.usermodel.Cell rowCell, CellStyle cellStyle, NumberStyleInfo numberStyleInfo, ExcelSheetWriterContext writerContext) {
        DataFormat dataFormat = rowCell.getSheet().getWorkbook().createDataFormat();
        if (numberStyleInfo.custom() && !numberStyleInfo.customFormat().isEmpty()) {
            cellStyle.setDataFormat(dataFormat.getFormat(numberStyleInfo.customFormat()));
        }
        else if (numberStyleInfo.date()) {
            cellStyle.setDataFormat(dataFormat.getFormat(numberStyleInfo.dateFormat().format()));
        }
        else if (numberStyleInfo.number()) {
            cellStyle.setDataFormat(dataFormat.getFormat(numberStyleInfo.numberFormat().format()));
        }
        else  {
            cellStyle.setDataFormat(dataFormat.getFormat(numberStyleInfo.categoryFormat().format()));
        }
    }

    private static DataCellStyleInfo extractDataCellStyleInfo(DataCellStyle dataCellStyle) {
        if(dataCellStyle == null) {
            return null;
        }
        DataCellStyleInfo dataCellStyleInfo = new DataCellStyleInfo();
        dataCellStyleInfo.setConditional(dataCellStyle.conditional());
        dataCellStyleInfo.setCondition(dataCellStyle.condition());
        dataCellStyleInfo.setBorderStyle(dataCellStyle.borderStyle());
        dataCellStyleInfo.setBorderColor(dataCellStyle.borderColor());
        dataCellStyleInfo.setApplyDefaultStyles(dataCellStyle.applyDefaultStyles());
        dataCellStyleInfo.setCustomTask(dataCellStyle.customTask());
        dataCellStyleInfo.setBackgroundColor(dataCellStyle.backgroundColor());
        dataCellStyleInfo.setFillPattern(dataCellStyle.fillPattern());
        dataCellStyleInfo.setForegroundColor(dataCellStyle.foregroundColor());
        dataCellStyleInfo.setHasBackgroundColor(dataCellStyle.hasBackgroundColor());
        dataCellStyleInfo.setHasBorderColor(dataCellStyle.hasBorderColor());
        dataCellStyleInfo.setHasBorderStyle(dataCellStyle.hasBorderStyle());
        dataCellStyleInfo.setHasForegroundColor(dataCellStyle.hasForegroundColor());
        dataCellStyleInfo.setColumnWidthInUnits(dataCellStyle.columnWidthInUnits());
        dataCellStyleInfo.setWrapText(dataCellStyle.wrapText());
        dataCellStyleInfo.setIgnoreStyleTemplate(dataCellStyle.ignoreStyleTemplate());
        StyleTemplateCellInfo styleTemplateCellInfo = extractStyleTemplateCellInfo(dataCellStyle.styleTemplateCell());
        dataCellStyleInfo.setStyleTemplateCellInfo(styleTemplateCellInfo);
        dataCellStyleInfo.setTextStyleInfo(extractTextStyleInfo(dataCellStyle.textStyle()));
        dataCellStyleInfo.setCustomTextStyle(dataCellStyle.customTextStyle());
        return dataCellStyleInfo;
    }

    public static DataCellStyle extractDataCellStyle(Field annotatedField, Object annotatedClassObject) {
        DataCellStyle dataCellStyle = annotatedField.getAnnotation(DataCellStyle.class);
        if(dataCellStyle == null) {
            dataCellStyle = annotatedClassObject.getClass().getAnnotation(DataCellStyle.class);
        }
        return dataCellStyle;
    }

    private static StyleTemplateCellInfo extractStyleTemplateCellInfo(StyleTemplateCell styleTemplateCell) {
        if(styleTemplateCell == null) {
            return null;
        }
        StyleTemplateCellInfo styleTemplateCellInfo = new StyleTemplateCellInfo();
        styleTemplateCellInfo.setColumn(styleTemplateCell.column());
        styleTemplateCellInfo.setRow(styleTemplateCell.row());
        return styleTemplateCellInfo;
    }

    public static TextStyleInfo extractTextStyleInfo(TextStyle textStyle) {
        if(textStyle == null) {
            return null;
        }
        TextStyleInfo textStyleInfo = new TextStyleInfo();
        textStyleInfo.setBold(textStyle.bold());
        textStyleInfo.setColor(textStyle.color());
        textStyleInfo.setItalic(textStyle.italic());
        textStyleInfo.setStrikeout(textStyle.strikeout());
        textStyleInfo.setFontName(textStyle.fontName());
        textStyleInfo.setFontHeight(textStyle.fontHeight());
        return textStyleInfo;
    }

    public static NumberStyleInfo extractNumberStyleInfo(NumberStyle numberStyle) {
        if(numberStyle == null) {
            return null;
        }
        NumberStyleInfo numberStyleInfo = new NumberStyleInfo();
        numberStyleInfo.setGeneral(numberStyle.general());
        numberStyleInfo.setNumber(numberStyle.number());
        numberStyleInfo.setCurrency(numberStyle.currency());
        numberStyleInfo.setAccounting(numberStyle.accounting());
        numberStyleInfo.setDate(numberStyle.date());
        numberStyleInfo.setTime(numberStyle.time());
        numberStyleInfo.setPercentage(numberStyle.percentage());
        numberStyleInfo.setFraction(numberStyle.fraction());
        numberStyleInfo.setScientific(numberStyle.scientific());
        numberStyleInfo.setText(numberStyle.text());
        numberStyleInfo.setCustom(numberStyle.custom());
        numberStyleInfo.setCategoryFormat(numberStyle.categoryFormat());
        numberStyleInfo.setCustomFormat(numberStyle.customFormat());
        numberStyleInfo.setDateFormat(numberStyle.dateFormat());
        numberStyleInfo.setNumberFormat(numberStyle.numberFormat());
        if(numberStyleInfo.getCategoryFormat() == null) {
            numberStyleInfo.setCategoryFormat(NumberCategoryFormat.GENERAL);
        }
        if(numberStyleInfo.getDateFormat() == null) {
            numberStyleInfo.setDateFormat(DateFormatPattern.SLASH_MM_DD_YYYY);
        }
        if(numberStyleInfo.getNumberFormat() == null) {
            numberStyleInfo.setNumberFormat(NumberPrecisionFormat.TWO_DECIMAL);
        }
        return numberStyleInfo;
    }

    public static void clearCellStyles(CellStyle defaultDataCellStyle) {
        clearBackgroundColor(defaultDataCellStyle);
    }
    
    private static void clearBackgroundColor(CellStyle defaultDataCellStyle) {
        defaultDataCellStyle.setFillPattern(FillPatternType.NO_FILL);
        defaultDataCellStyle.setFillForegroundColor(IndexedColors.AUTOMATIC.getIndex());
    }
}
