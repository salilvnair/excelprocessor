package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.HeaderCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import org.apache.poi.ss.usermodel.CellStyle;

import java.lang.reflect.Field;

public class HeaderCellStyleWriterUtil {
    private HeaderCellStyleWriterUtil(){}


    public static void applyCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext writerContext) {
        applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
    }
    public static void applyStaticCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        HeaderCellStyle headerCellStyle = extractHeaderCellStyle(cellField, writerContext.getSheetDataObj());
        if(headerCellStyle == null) {
            return;
        }
        CellStyle cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();

        cellStyle.setWrapText(headerCellStyle.wrapText());
        if(headerCellStyle.hasBorderStyle()) {
            cellStyle.setBorderTop(headerCellStyle.borderStyle());
            cellStyle.setBorderRight(headerCellStyle.borderStyle());
            cellStyle.setBorderBottom(headerCellStyle.borderStyle());
            cellStyle.setBorderLeft(headerCellStyle.borderStyle());
        }
        if(headerCellStyle.hasBorderColor()) {
            cellStyle.setTopBorderColor(headerCellStyle.borderColor().getIndex());
            cellStyle.setRightBorderColor(headerCellStyle.borderColor().getIndex());
            cellStyle.setBottomBorderColor(headerCellStyle.borderColor().getIndex());
            cellStyle.setLeftBorderColor(headerCellStyle.borderColor().getIndex());
        }
        if(headerCellStyle.hasBackgroundColor()) {
            cellStyle.setFillPattern(headerCellStyle.fillPattern());
            cellStyle.setFillForegroundColor(headerCellStyle.foregroundColor().getIndex());
            cellStyle.setFillBackgroundColor(headerCellStyle.foregroundColor().getIndex());
        }
        if(headerCellStyle.hasForegroundColor()) {
            cellStyle.setFillPattern(headerCellStyle.fillPattern());
            cellStyle.setFillForegroundColor(headerCellStyle.foregroundColor().getIndex());
        }
        rowCell.setCellStyle(cellStyle);
    }

    public static HeaderCellStyle extractHeaderCellStyle(Field annotatedField, Object annotatedClassObject) {
        HeaderCellStyle headerCellStyle = annotatedField.getAnnotation(HeaderCellStyle.class);
        if(headerCellStyle == null) {
            headerCellStyle = annotatedClassObject.getClass().getAnnotation(HeaderCellStyle.class);
        }
        return headerCellStyle;
    }
    
}
