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
import com.github.salilvnair.excelprocessor.v2.processor.helper.style.CellStyleResolver;
import com.github.salilvnair.excelprocessor.v2.processor.helper.style.CellStyleSpec;
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
                dataCellStyleInfo = classStyleInfo(dataCellStyle);
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
        CellStyleResolver.runWithPrivateStyle(rowCell, writerContext, () -> {
            if(!StringUtils.isEmpty(dataCellStyle.customTask())) {
                ExcelCellStyleTaskExecutor.execute(dataCellStyle.customTask(), sheet, writerContext);
            }
            else {
                for (String customTask : dataCellStyle.customTasks()) {
                    ExcelCellStyleTaskExecutor.execute(customTask, sheet, writerContext);
                }
            }
        });
    }

    private static void executeCustomTask(DataCellStyleInfo dataCellStyle, Sheet sheet, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        CellStyleResolver.runWithPrivateStyle(rowCell, writerContext, () -> {
            if(!StringUtils.isEmpty(dataCellStyle.getCustomTask())) {
                ExcelCellStyleTaskExecutor.execute(dataCellStyle.getCustomTask(), sheet, writerContext);
            }
            else {
                for (String customTask : dataCellStyle.getCustomTasks()) {
                    ExcelCellStyleTaskExecutor.execute(customTask, sheet, writerContext);
                }
            }
        });
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
        applyResolvedStyle(sheet, fieldStyleInfo(dataCellStyle), rowCell, writerContext);
    }

    /** annotation -> info caches: annotations are immutable, so the derived infos can be shared */
    private static final Map<DataCellStyle, DataCellStyleInfo> FIELD_STYLE_INFO = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<DataCellStyle, DataCellStyleInfo> CLASS_STYLE_INFO = new java.util.concurrent.ConcurrentHashMap<>();

    private static DataCellStyleInfo fieldStyleInfo(DataCellStyle dataCellStyle) {
        return FIELD_STYLE_INFO.computeIfAbsent(dataCellStyle, a -> {
            DataCellStyleInfo info = extractDataCellStyleInfo(a);
            info.setNumberStyleInfo(extractNumberStyleInfo(a.numberStyle()));
            return info;
        });
    }

    private static DataCellStyleInfo classStyleInfo(DataCellStyle dataCellStyle) {
        return dataCellStyle == null ? null : CLASS_STYLE_INFO.computeIfAbsent(dataCellStyle, DataCellStyleWriterUtil::extractDataCellStyleInfo);
    }

    public static void applyDynamicCellStyles(Sheet sheet, String header, DataCellStyleInfo dataCellStyleInfo, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        if(dataCellStyleInfo == null) {
            return;
        }
        applyResolvedStyle(sheet, dataCellStyleInfo, rowCell, writerContext);
    }

    /**
     * Builds the effective style of the cell as an immutable {@link CellStyleSpec} and applies the
     * shared workbook style for it. Equal specs reuse one style, so the workbook style count stays
     * proportional to the number of distinct styles instead of the number of cells.
     */
    private static void applyResolvedStyle(Sheet sheet, DataCellStyleInfo info, org.apache.poi.ss.usermodel.Cell rowCell, ExcelSheetWriterContext writerContext) {
        CellStyleSpec.Builder spec = CellStyleSpec.builder();
        CellStyle cellStyle;
        if(writerContext.styleTemplate()!=null && !info.isIgnoreStyleTemplate()) {
            spec.templateStyle(CellStyleResolver.templateStyle(writerContext, sheet, info.getStyleTemplateCellInfo(), rowCell));
            cellStyle = CellStyleResolver.apply(rowCell, buildSpec(spec, info), writerContext);
        }
        else {
            // hot path: the same (style info, base style) pair resolves to the same shared style
            int baseStyleIndex = CellStyleResolver.baseStyleIndex(rowCell);
            org.apache.poi.ss.usermodel.Workbook workbook = rowCell.getSheet().getWorkbook();
            cellStyle = writerContext.styleRegistry().memo(workbook, info, baseStyleIndex);
            if(cellStyle == null) {
                spec.baseStyleIndex(baseStyleIndex);
                cellStyle = writerContext.styleRegistry().resolve(workbook, buildSpec(spec, info));
                writerContext.styleRegistry().putMemo(workbook, info, baseStyleIndex, cellStyle);
            }
            rowCell.setCellStyle(cellStyle);
        }
        // kept for backward compatibility with tasks reading context.defaultDataCellStyle();
        // the style is shared and must not be mutated.
        writerContext.setDefaultDataCellStyle(cellStyle);
    }

    private static CellStyleSpec buildSpec(CellStyleSpec.Builder spec, DataCellStyleInfo info) {
        spec.wrapText(info.isWrapText());
        if(info.isHasBorderStyle()) {
            spec.border(info.getBorderStyle());
        }
        if(info.isHasBorderColor()) {
            spec.borderColor(info.getBorderColor().getIndex());
        }
        if(info.isHasBackgroundColor()) {
            spec.fillPattern(info.getFillPattern());
            spec.fillForegroundColor(info.getBackgroundColor().getIndex());
            spec.fillBackgroundColor(info.getBackgroundColor().getIndex());
        }
        if(info.isHasForegroundColor()) {
            spec.fillPattern(info.getFillPattern());
            spec.fillForegroundColor(info.getForegroundColor().getIndex());
        }
        if(info.isCustomTextStyle() && info.getTextStyleInfo()!=null) {
            spec.font(CellStyleResolver.font(info.getTextStyleInfo()));
        }
        spec.dataFormat(CellStyleResolver.dataFormat(info.getNumberStyleInfo()));
        return spec.build();
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
