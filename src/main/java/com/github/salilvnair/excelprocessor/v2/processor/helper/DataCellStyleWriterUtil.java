package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.StyleTemplateCell;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.StyleTemplateCellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.task.helper.ExcelCellStyleTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.CellStyle;
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
            applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
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
        writerContext.setRowCell(rowCell);
        writerContext.setCellValue(fieldValue);
        if(dataCellStyleInfo.isConditional()) {
            applyConditionalDynamicCellStyle(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
        else if(!StringUtils.isEmpty(dataCellStyleInfo.getCustomTask()) || dataCellStyleInfo.getCustomTasks().length > 0) {
            executeCustomTaskWithDynamicCellStyle(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
        else {
            applyStaticDynamicCellStyles(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
    }

    private static DataCellStyleInfo extractDataCellStyleInfo(DataCellStyle dataCellStyle) {
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
        return dataCellStyleInfo;
    }

    private static void executeCustomTaskWithCellStyle(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        if(dataCellStyle.applyDefaultStyles()) {
            applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
        executeCustomTask(dataCellStyle, sheet, cell, rowCell, cellField, writerContext);
    }

    private static void executeCustomTaskWithDynamicCellStyle(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        writerContext.setHeader(header);
        if(dataCellStyleInfo.isApplyDefaultStyles()) {
            applyStaticDynamicCellStyles(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
        executeCustomTask(dataCellStyleInfo, sheet, rowCell, writerContext);
    }

    private static void executeCustomTask(DataCellStyle dataCellStyle, Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        if(!StringUtils.isEmpty(dataCellStyle.customTask())) {
            ExcelCellStyleTaskExecutor.execute(dataCellStyle.customTask(), sheet.excelTask(), writerContext);
        }
        else {
            for (String customTask : dataCellStyle.customTasks()) {
                ExcelCellStyleTaskExecutor.execute(customTask, sheet.excelTask(), writerContext);
            }
        }
    }

    private static void executeCustomTask(DataCellStyleInfo dataCellStyle, Sheet sheet, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(!StringUtils.isEmpty(dataCellStyle.getCustomTask())) {
            ExcelCellStyleTaskExecutor.execute(dataCellStyle.getCustomTask(), sheet.excelTask(), writerContext);
        }
        else {
            for (String customTask : dataCellStyle.getCustomTasks()) {
                ExcelCellStyleTaskExecutor.execute(customTask, sheet.excelTask(), writerContext);
            }
        }
    }

    private static void applyConditionalCellStyle(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle =  extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        Object object = ExcelCellStyleTaskExecutor.execute(dataCellStyle.condition(), sheet.excelTask(), writerContext);
        if(ObjectUtil.nonNullOrBooleanTrue(object)) {
            applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
    }

    private static void applyConditionalDynamicCellStyle(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        writerContext.setHeader(header);
        Object object = ExcelCellStyleTaskExecutor.execute(dataCellStyleInfo.getCondition(), sheet.excelTask(), writerContext);
        if(ObjectUtil.nonNullOrBooleanTrue(object)) {
            applyStaticDynamicCellStyles(sheet, header, dataCellStyleInfo, rowCell, writerContext);
        }
    }

    public static void applyStaticCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle =  extractDataCellStyle(cellField, writerContext.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null) {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
            StyleTemplateCell styleTemplateCell = dataCellStyle.styleTemplateCell();
            int rowIndex = styleTemplateCell.row()  - 1;
            int columnIndex = ExcelSheetReader.toIndentNumber(styleTemplateCell.column())  - 1;
            CellStyle templateCellStyle = writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getCellStyle();
            cellStyle.cloneStyleFrom(templateCellStyle);
            // Copy cell width
            rowCell.getSheet().setColumnWidth(rowCell.getColumnIndex(), writerContext.styleTemplate().getSheet(sheet.value()).getColumnWidth(writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getColumnIndex()));
            // Copy text wrapping
            cellStyle.setWrapText(templateCellStyle.getWrapText());
        }
        else {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
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
        rowCell.setCellStyle(cellStyle);
    }


    public static void applyStaticDynamicCellStyles(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null) {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
            StyleTemplateCellInfo styleTemplateCellInfo = dataCellStyleInfo.getStyleTemplateCellInfo();
            int rowIndex = styleTemplateCellInfo.getRow()  - 1;
            int columnIndex = ExcelSheetReader.toIndentNumber(styleTemplateCellInfo.getColumn())  - 1;
            CellStyle templateCellStyle = writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getCellStyle();
            cellStyle.cloneStyleFrom(templateCellStyle);
            // Copy cell width
            rowCell.getSheet().setColumnWidth(rowCell.getColumnIndex(), writerContext.styleTemplate().getSheet(sheet.value()).getColumnWidth(writerContext.styleTemplate().getSheet(sheet.value()).getRow(rowIndex).getCell(columnIndex).getColumnIndex()));
            // Copy text wrapping
            cellStyle.setWrapText(templateCellStyle.getWrapText());
        }
        else {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
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
        rowCell.setCellStyle(cellStyle);
    }

    public static DataCellStyle extractDataCellStyle(Field annotatedField, Object annotatedClassObject) {
        DataCellStyle dataCellStyle = annotatedField.getAnnotation(DataCellStyle.class);
        if(dataCellStyle == null) {
            dataCellStyle = annotatedClassObject.getClass().getAnnotation(DataCellStyle.class);
        }
        return dataCellStyle;
    }
    
}
