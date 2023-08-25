package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.v2.annotation.*;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.StyleTemplateCellInfo;
import com.github.salilvnair.excelprocessor.v2.model.TextStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.lang.reflect.Field;
import java.util.Map;

public class HeaderCellStyleWriterUtil {
    private HeaderCellStyleWriterUtil(){}


    public static void applyCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext writerContext) {
        applyStaticCellStyles(sheet, cell, rowCell, cellField, writerContext);
    }

    public static void applyDynamicCellStyles(Sheet sheet, String header, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo = writerContext.getDynamicHeaderCellStyleInfo();
        HeaderCellStyleInfo headerCellStyleInfo = null;
        if(dynamicHeaderCellStyleInfo == null || !dynamicHeaderCellStyleInfo.containsKey(header)) {
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
            headerCellStyleInfo = dynamicHeaderCellStyleInfo.get(header);
        }

        applyStaticDynamicCellStyles(sheet, header, headerCellStyleInfo, rowCell, writerContext);
    }


    public static void applyStaticCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, ExcelSheetWriterContext writerContext) {
        HeaderCellStyle headerCellStyle = extractHeaderCellStyle(cellField, writerContext);
        if(headerCellStyle == null) {
            return;
        }
        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null && !headerCellStyle.ignoreStyleTemplate()) {
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
        cellStyle.setHidden(headerCellStyle.hide());
        if(headerCellStyle.hide()) {
            rowCell.getSheet().setColumnHidden(rowCell.getColumnIndex(), true);
        }
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
        if(headerCellStyle.customTextStyle()) {
            applyTextStyleIfApplicable(rowCell, cellStyle, extractTextStyleInfo(headerCellStyle.textStyle()));
        }
        rowCell.setCellStyle(cellStyle);
    }

    public static void applyStaticDynamicCellStyles(Sheet sheet, String header, HeaderCellStyleInfo headerCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(headerCellStyleInfo == null) {
            return;
        }

        CellStyle cellStyle = null;

        if(writerContext.styleTemplate()!=null && !headerCellStyleInfo.isIgnoreStyleTemplate()) {
            cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();
            StyleTemplateCellInfo styleTemplateCellInfo = headerCellStyleInfo.getStyleTemplateCellInfo();
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

        cellStyle.setWrapText(headerCellStyleInfo.isWrapText());
        cellStyle.setHidden(headerCellStyleInfo.isHide());
        if(headerCellStyleInfo.isHide()) {
            rowCell.getSheet().setColumnHidden(rowCell.getColumnIndex(), true);
        }
        if(headerCellStyleInfo.isHasBorderStyle()) {
            cellStyle.setBorderTop(headerCellStyleInfo.getBorderStyle());
            cellStyle.setBorderRight(headerCellStyleInfo.getBorderStyle());
            cellStyle.setBorderBottom(headerCellStyleInfo.getBorderStyle());
            cellStyle.setBorderLeft(headerCellStyleInfo.getBorderStyle());
        }
        if(headerCellStyleInfo.isHasBorderColor()) {
            cellStyle.setTopBorderColor(headerCellStyleInfo.getBorderColor().getIndex());
            cellStyle.setRightBorderColor(headerCellStyleInfo.getBorderColor().getIndex());
            cellStyle.setBottomBorderColor(headerCellStyleInfo.getBorderColor().getIndex());
            cellStyle.setLeftBorderColor(headerCellStyleInfo.getBorderColor().getIndex());
        }

        if(headerCellStyleInfo.isHasBackgroundColor()) {
            cellStyle.setFillPattern(headerCellStyleInfo.getFillPattern());
            cellStyle.setFillForegroundColor(headerCellStyleInfo.getBackgroundColor().getIndex());
            cellStyle.setFillBackgroundColor(headerCellStyleInfo.getBackgroundColor().getIndex());
        }

        if(headerCellStyleInfo.isHasForegroundColor()) {
            cellStyle.setFillPattern(headerCellStyleInfo.getFillPattern());
            cellStyle.setFillForegroundColor(headerCellStyleInfo.getForegroundColor().getIndex());
        }
        if(headerCellStyleInfo.isCustomTextStyle() && headerCellStyleInfo.getTextStyleInfo()!=null) {
            applyTextStyleIfApplicable(rowCell, cellStyle, headerCellStyleInfo.getTextStyleInfo());
        }
        rowCell.setCellStyle(cellStyle);
    }

    private static void applyTextStyleIfApplicable(org.apache.poi.ss.usermodel.Cell rowCell, CellStyle cellStyle, TextStyleInfo textStyleInfo) {
        Font font = rowCell.getSheet().getWorkbook().createFont();
        font.setBold(textStyleInfo.isBold());
        font.setItalic(textStyleInfo.isItalic());
        font.setColor(textStyleInfo.getColor().getIndex());
        font.setStrikeout(textStyleInfo.isStrikeout());
        if(StringUtils.isNotEmpty(textStyleInfo.getFontName())) {
            font.setFontName(textStyleInfo.getFontName());
        }
        if(textStyleInfo.getFontHeight() != -1) {
            font.setFontHeight(textStyleInfo.getFontHeight());
        }
        cellStyle.setFont(font);
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
        headerCellStyleInfo.setConditional(headerCellStyle.conditional());
        headerCellStyleInfo.setCondition(headerCellStyle.condition());
        headerCellStyleInfo.setBorderStyle(headerCellStyle.borderStyle());
        headerCellStyleInfo.setBorderColor(headerCellStyle.borderColor());
        headerCellStyleInfo.setCustomTask(headerCellStyle.customTask());
        headerCellStyleInfo.setBackgroundColor(headerCellStyle.backgroundColor());
        headerCellStyleInfo.setFillPattern(headerCellStyle.fillPattern());
        headerCellStyleInfo.setForegroundColor(headerCellStyle.foregroundColor());
        headerCellStyleInfo.setHasBackgroundColor(headerCellStyle.hasBackgroundColor());
        headerCellStyleInfo.setHasBorderColor(headerCellStyle.hasBorderColor());
        headerCellStyleInfo.setHasBorderStyle(headerCellStyle.hasBorderStyle());
        headerCellStyleInfo.setHasForegroundColor(headerCellStyle.hasForegroundColor());
        headerCellStyleInfo.setColumnWidthInUnits(headerCellStyle.columnWidthInUnits());
        headerCellStyleInfo.setWrapText(headerCellStyle.wrapText());
        headerCellStyleInfo.setIgnoreStyleTemplate(headerCellStyle.ignoreStyleTemplate());
        headerCellStyleInfo.setTextStyleInfo(extractTextStyleInfo(headerCellStyle.textStyle()));
        StyleTemplateCellInfo styleTemplateCellInfo = extractStyleTemplateCellInfo(headerCellStyle.styleTemplateCell());
        headerCellStyleInfo.setCustomTextStyle(headerCellStyle.customTextStyle());
        headerCellStyleInfo.setStyleTemplateCellInfo(styleTemplateCellInfo);
        return headerCellStyleInfo;
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
}
