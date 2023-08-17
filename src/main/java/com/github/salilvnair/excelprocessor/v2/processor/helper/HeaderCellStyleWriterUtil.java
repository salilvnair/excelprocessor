package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.v2.annotation.*;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.StyleTemplateCellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;

import java.lang.reflect.Field;
import java.util.Map;

public class HeaderCellStyleWriterUtil {
    private HeaderCellStyleWriterUtil(){}


    public static void applyCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext writerContext) {
        applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
    }

    public static void applyDynamicCellStyles(Sheet sheet, String header, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        Map<String, HeaderCellStyleInfo> dynamicHeaderDataCellStyleInfo = writerContext.getDynamicHeaderCellStyleInfo();
        HeaderCellStyleInfo headerCellStyleInfo = null;
        if(dynamicHeaderDataCellStyleInfo == null || !dynamicHeaderDataCellStyleInfo.containsKey(header)) {
            BaseSheet sheetDataObj = writerContext.getSheetDataObj();
            if(sheetDataObj != null) {
                HeaderCellStyle headerCellStyle  = sheetDataObj.getClass().getAnnotation(HeaderCellStyle.class);
                headerCellStyleInfo = extractHeaderCellStyleInfo(headerCellStyle);
            }
            else {
                return;
            }
        }
        else {
            headerCellStyleInfo = dynamicHeaderDataCellStyleInfo.get(header);
        }

        applyStaticDynamicCellStyles(sheet, header, headerCellStyleInfo, rowCell, writerContext);
    }


    public static void applyStaticCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        HeaderCellStyle headerCellStyle = extractHeaderCellStyle(cellField, writerContext);
        if(headerCellStyle == null) {
            return;
        }
        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null) {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
            StyleTemplateCell styleTemplateCell = headerCellStyle.styleTemplateCell();
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

    public static void applyStaticDynamicCellStyles(Sheet sheet, String header, HeaderCellStyleInfo headerCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(headerCellStyleInfo == null) {
            return;
        }

        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null) {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
            StyleTemplateCellInfo styleTemplateCellInfo = headerCellStyleInfo.styleTemplateCellInfo;
            int rowIndex = styleTemplateCellInfo.row  - 1;
            int columnIndex = ExcelSheetReader.toIndentNumber(styleTemplateCellInfo.column)  - 1;
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

        cellStyle.setWrapText(headerCellStyleInfo.wrapText);
        if(headerCellStyleInfo.hasBorderStyle) {
            cellStyle.setBorderTop(headerCellStyleInfo.borderStyle);
            cellStyle.setBorderRight(headerCellStyleInfo.borderStyle);
            cellStyle.setBorderBottom(headerCellStyleInfo.borderStyle);
            cellStyle.setBorderLeft(headerCellStyleInfo.borderStyle);
        }
        if(headerCellStyleInfo.hasBorderColor) {
            cellStyle.setTopBorderColor(headerCellStyleInfo.borderColor.getIndex());
            cellStyle.setRightBorderColor(headerCellStyleInfo.borderColor.getIndex());
            cellStyle.setBottomBorderColor(headerCellStyleInfo.borderColor.getIndex());
            cellStyle.setLeftBorderColor(headerCellStyleInfo.borderColor.getIndex());
        }

        if(headerCellStyleInfo.hasBackgroundColor) {
            cellStyle.setFillPattern(headerCellStyleInfo.fillPattern);
            cellStyle.setFillForegroundColor(headerCellStyleInfo.backgroundColor.getIndex());
            cellStyle.setFillBackgroundColor(headerCellStyleInfo.backgroundColor.getIndex());
        }

        if(headerCellStyleInfo.hasForegroundColor) {
            cellStyle.setFillPattern(headerCellStyleInfo.fillPattern);
            cellStyle.setFillForegroundColor(headerCellStyleInfo.foregroundColor.getIndex());
        }
        rowCell.setCellStyle(cellStyle);
    }

    public static HeaderCellStyle extractHeaderCellStyle(Field annotatedField, ExcelSheetWriterContext writerContext) {
        HeaderCellStyle headerCellStyle = annotatedField.getAnnotation(HeaderCellStyle.class);
        if(headerCellStyle == null) {
            if(CollectionUtils.isNotEmpty(writerContext.getSheetData())) {
                headerCellStyle = writerContext.getSheetData().get(0).getClass().getAnnotation(HeaderCellStyle.class);
            }
        }
        return headerCellStyle;
    }

    private static HeaderCellStyleInfo extractHeaderCellStyleInfo(HeaderCellStyle headerCellStyle) {
        if(headerCellStyle == null) {
            return null;
        }
        HeaderCellStyleInfo headerCellStyleInfo = new HeaderCellStyleInfo();
        headerCellStyleInfo.conditional = headerCellStyle.conditional();
        headerCellStyleInfo.condition = headerCellStyle.condition();
        headerCellStyleInfo.borderStyle = headerCellStyle.borderStyle();
        headerCellStyleInfo.borderColor = headerCellStyle.borderColor();
        headerCellStyleInfo.customTask = headerCellStyle.customTask();
        headerCellStyleInfo.backgroundColor = headerCellStyle.backgroundColor();
        headerCellStyleInfo.fillPattern = headerCellStyle.fillPattern();
        headerCellStyleInfo.foregroundColor = headerCellStyle.foregroundColor();
        headerCellStyleInfo.hasBackgroundColor = headerCellStyle.hasBackgroundColor();
        headerCellStyleInfo.hasBorderColor = headerCellStyle.hasBorderColor();
        headerCellStyleInfo.hasBorderStyle = headerCellStyle.hasBorderStyle();
        headerCellStyleInfo.hasForegroundColor = headerCellStyle.hasForegroundColor();
        headerCellStyleInfo.columnWidthInUnits = headerCellStyle.columnWidthInUnits();
        headerCellStyleInfo.wrapText = headerCellStyle.wrapText();
        return headerCellStyleInfo;
    }

    private static StyleTemplateCellInfo extractStyleTemplateCellInfo(StyleTemplateCell styleTemplateCell) {
        if(styleTemplateCell == null) {
            return null;
        }
        StyleTemplateCellInfo styleTemplateCellInfo = new StyleTemplateCellInfo();
        styleTemplateCellInfo.column = styleTemplateCell.column();
        styleTemplateCellInfo.row = styleTemplateCell.row();
        return styleTemplateCellInfo;
    }
    
}
