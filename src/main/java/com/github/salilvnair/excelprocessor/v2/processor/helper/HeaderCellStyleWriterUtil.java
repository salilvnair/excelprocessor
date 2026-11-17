package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.v2.annotation.*;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.NumberStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.StyleTemplateCellInfo;
import com.github.salilvnair.excelprocessor.v2.model.TextStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.style.CellStyleResolver;
import com.github.salilvnair.excelprocessor.v2.processor.helper.style.CellStyleSpec;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.DateFormatPattern;
import com.github.salilvnair.excelprocessor.v2.type.NumberCategoryFormat;
import com.github.salilvnair.excelprocessor.v2.type.NumberPrecisionFormat;
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
        CellStyleSpec.Builder spec = CellStyleSpec.builder();
        if(writerContext.styleTemplate()!=null && !headerCellStyle.ignoreStyleTemplate()) {
            StyleTemplateCell styleTemplateCell = headerCellStyle.styleTemplateCell();
            spec.templateStyle(CellStyleResolver.templateStyle(writerContext, sheet, styleTemplateCell.row(), styleTemplateCell.column(), rowCell));
        }
        spec.wrapText(headerCellStyle.wrapText());
        spec.hidden(headerCellStyle.hide());
        if(headerCellStyle.hide()) {
            rowCell.getSheet().setColumnHidden(rowCell.getColumnIndex(), true);
        }
        if(headerCellStyle.hasBorderStyle()) {
            spec.border(headerCellStyle.borderStyle());
        }
        if(headerCellStyle.hasBorderColor()) {
            spec.borderColor(headerCellStyle.borderColor().getIndex());
        }
        // note: for static headers hasBackgroundColor has always applied foregroundColor (kept as-is)
        if(headerCellStyle.hasBackgroundColor()) {
            spec.fillPattern(headerCellStyle.fillPattern());
            spec.fillForegroundColor(headerCellStyle.foregroundColor().getIndex());
            spec.fillBackgroundColor(headerCellStyle.foregroundColor().getIndex());
        }
        if(headerCellStyle.hasForegroundColor()) {
            spec.fillPattern(headerCellStyle.fillPattern());
            spec.fillForegroundColor(headerCellStyle.foregroundColor().getIndex());
        }
        if(headerCellStyle.customTextStyle()) {
            spec.font(CellStyleResolver.font(extractTextStyleInfo(headerCellStyle.textStyle())));
        }
        CellStyleResolver.apply(rowCell, spec.build(), writerContext);
    }

    public static void applyStaticDynamicCellStyles(Sheet sheet, String header, HeaderCellStyleInfo headerCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(headerCellStyleInfo == null) {
            return;
        }
        CellStyleSpec.Builder spec = CellStyleSpec.builder();
        if(writerContext.styleTemplate()!=null && !headerCellStyleInfo.isIgnoreStyleTemplate()) {
            spec.templateStyle(CellStyleResolver.templateStyle(writerContext, sheet, headerCellStyleInfo.getStyleTemplateCellInfo(), rowCell));
        }
        else {
            spec.baseStyleIndex(CellStyleResolver.baseStyleIndex(rowCell));
        }
        spec.wrapText(headerCellStyleInfo.isWrapText());
        spec.hidden(headerCellStyleInfo.isHide());
        if(headerCellStyleInfo.isHide()) {
            rowCell.getSheet().setColumnHidden(rowCell.getColumnIndex(), true);
        }
        if(headerCellStyleInfo.isHasBorderStyle()) {
            spec.border(headerCellStyleInfo.getBorderStyle());
        }
        if(headerCellStyleInfo.isHasBorderColor()) {
            spec.borderColor(headerCellStyleInfo.getBorderColor().getIndex());
        }
        if(headerCellStyleInfo.isHasBackgroundColor()) {
            spec.fillPattern(headerCellStyleInfo.getFillPattern());
            spec.fillForegroundColor(headerCellStyleInfo.getBackgroundColor().getIndex());
            spec.fillBackgroundColor(headerCellStyleInfo.getBackgroundColor().getIndex());
        }
        if(headerCellStyleInfo.isHasForegroundColor()) {
            spec.fillPattern(headerCellStyleInfo.getFillPattern());
            spec.fillForegroundColor(headerCellStyleInfo.getForegroundColor().getIndex());
        }
        if(headerCellStyleInfo.isCustomTextStyle() && headerCellStyleInfo.getTextStyleInfo()!=null) {
            spec.font(CellStyleResolver.font(headerCellStyleInfo.getTextStyleInfo()));
        }
        CellStyleResolver.apply(rowCell, spec.build(), writerContext);
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
}
