package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.task.helper.ExcelCellStyleTaskExecutor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellUtil;

import java.lang.reflect.Field;

public class DataCellStyleWriterUtil {
    private DataCellStyleWriterUtil(){}


    public static void applyCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = cellField.getAnnotation(DataCellStyle.class);
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

    private static void executeCustomTaskWithCellStyle(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = cellField.getAnnotation(DataCellStyle.class);
        if(dataCellStyle == null) {
            return;
        }
        if(dataCellStyle.applyDefaultStyles()) {
            applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
        executeCustomTask(dataCellStyle, sheet, cell, rowCell, cellField, writerContext);
    }

    private static void executeCustomTask(DataCellStyle dataCellStyle, Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        if(!StringUtils.isEmpty(dataCellStyle.customTask())) {
            ExcelCellStyleTaskExecutor.execute(dataCellStyle.customTask(), sheet.excelTaskValidator(), writerContext);
        }
        else {
            for (String customTask : dataCellStyle.customTasks()) {
                ExcelCellStyleTaskExecutor.execute(customTask, sheet.excelTaskValidator(), writerContext);
            }
        }
    }

    private static void applyConditionalCellStyle(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = cellField.getAnnotation(DataCellStyle.class);
        if(dataCellStyle == null) {
            return;
        }
        Object object = ExcelCellStyleTaskExecutor.execute(dataCellStyle.condition(), sheet.excelTaskValidator(), writerContext);
        if(ObjectUtil.nonNullOrBooleanTrue(object)) {
            applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
        }
    }

    public static void applyStaticCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        DataCellStyle dataCellStyle = cellField.getAnnotation(DataCellStyle.class);
        if(dataCellStyle == null) {
            return;
        }
        CellStyle cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();

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
    
}
