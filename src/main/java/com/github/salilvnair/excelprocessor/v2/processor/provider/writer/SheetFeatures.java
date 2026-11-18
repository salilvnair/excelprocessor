package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.AllowedValues;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.ConditionalFormat;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.helper.ValueCoercer;
import com.github.salilvnair.excelprocessor.v2.model.AllowedValuesInfo;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Sheet level output options of {@code @Sheet} and {@code @ConditionalFormat}: frozen header,
 * header height, dropdowns from allowed values, native conditional formatting, auto filter and
 * column auto sizing. Column positions come from the headers the writer recorded in
 * {@link ExcelSheetWriterContext#writtenHeaders()}, so they work for list, template and streaming
 * writes alike.
 */
final class SheetFeatures {
    /** hidden sheet holding dropdown lists too long (or with commas) for an inline list */
    static final String LIST_SHEET = "ExcelProcessorLists";
    /** Excel's limit for an inline list validation formula */
    private static final int MAX_INLINE_LIST_LENGTH = 255;

    private SheetFeatures() {}

    static void headerRow(Row row, Sheet sheet) {
        if (row != null && sheet.headerRowHeight() > 0) {
            row.setHeightInPoints(sheet.headerRowHeight());
        }
    }

    /** Rules that cover whole data columns; safe to apply before any data row is written. */
    static void applyLayout(Sheet sheet, org.apache.poi.ss.usermodel.Sheet workbookSheet, ExcelSheetWriterContext context) {
        Map<Integer, String> headers = context.writtenHeaders();
        if (sheet.vertical() || headers.isEmpty()) {
            return;
        }
        int headerRowIndex = sheet.headerRowAt() - 1;
        int firstDataRow = sheet.valueRowAt() > -1 ? sheet.valueRowAt() - 1 : headerRowIndex + 1;
        int lastRow = workbookSheet.getWorkbook().getSpreadsheetVersion().getLastRowIndex();
        if (sheet.freezeHeader()) {
            workbookSheet.createFreezePane(0, headerRowIndex + 1);
        }
        Map<String, Field> fields = fieldsByHeader(sheetClass(context));
        for (Map.Entry<Integer, String> entry : headers.entrySet()) {
            int column = entry.getKey();
            String header = entry.getValue();
            Field field = fields.get(header + "_" + ExcelSheetReader.toIndentName(column + 1));
            if (field == null) {
                field = fields.get(header);
            }
            CellRangeAddressList dataCells = new CellRangeAddressList(firstDataRow, lastRow, column, column);
            if (sheet.allowedValuesDropdown()) {
                addDropdown(workbookSheet, dataCells, field, header, context);
            }
            if (field != null) {
                ConditionalFormat[] formats = field.getAnnotationsByType(ConditionalFormat.class);
                if (formats.length > 0) {
                    addConditionalFormats(workbookSheet, new CellRangeAddress(firstDataRow, lastRow, column, column), firstDataRow, column, formats);
                }
            }
        }
    }

    /** Options that depend on the rows written: auto filter range and column widths. */
    static void complete(Sheet sheet, org.apache.poi.ss.usermodel.Sheet workbookSheet, ExcelSheetWriterContext context) {
        Map<Integer, String> headers = context.writtenHeaders();
        if (sheet.vertical() || headers.isEmpty()) {
            return;
        }
        int headerRowIndex = sheet.headerRowAt() - 1;
        int firstColumn = Collections.min(headers.keySet());
        int lastColumn = Collections.max(headers.keySet());
        if (sheet.autoFilter()) {
            int lastRow = Math.max(workbookSheet.getLastRowNum(), headerRowIndex);
            workbookSheet.setAutoFilter(new CellRangeAddress(headerRowIndex, lastRow, firstColumn, lastColumn));
        }
        if (sheet.autoSizeColumns()) {
            for (int column : headers.keySet()) {
                try {
                    workbookSheet.autoSizeColumn(column);
                }
                catch (IllegalStateException e) {
                    // SXSSF column not tracked (sheet created elsewhere); keep the width
                }
            }
        }
    }

    private static Class<?> sheetClass(ExcelSheetWriterContext context) {
        List<? extends BaseSheet> data = context.getSheetData();
        if (data != null && !data.isEmpty() && data.get(0) != null) {
            return data.get(0).getClass();
        }
        return context.getSheetDataObj() == null ? null : context.getSheetDataObj().getClass();
    }

    private static Map<String, Field> fieldsByHeader(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyMap();
        }
        Map<String, Field> fields = new HashMap<>();
        for (Field field : AnnotationUtil.getAnnotatedFields(clazz, Cell.class)) {
            Cell cell = field.getAnnotation(Cell.class);
            if (StringUtils.isNotEmpty(cell.column())) {
                fields.put(cell.value() + "_" + cell.column(), field);
            }
            fields.putIfAbsent(cell.value(), field);
        }
        return fields;
    }

    private static void addDropdown(org.apache.poi.ss.usermodel.Sheet workbookSheet, CellRangeAddressList dataCells, Field field, String header, ExcelSheetWriterContext context) {
        List<String> values = new ArrayList<>();
        int[] range = {};
        if (field != null) {
            AllowedValues allowedValues = field.getAnnotation(AllowedValues.class);
            if (allowedValues != null && !allowedValues.conditional()) {
                values.addAll(Arrays.asList(allowedValues.value()));
                values.addAll(dataSet(allowedValues.dataSetKey(), context));
                range = allowedValues.range();
            }
            else if (allowedValues == null && ValueCoercer.wrap(field.getType()).isEnum()) {
                values.addAll(ValueCoercer.enumLabels(field.getType()));
            }
        }
        else if (context.getHeaderKeyedCellValidationInfo() != null) {
            CellValidationInfo info = context.getHeaderKeyedCellValidationInfo().get(header);
            AllowedValuesInfo allowedValues = info == null ? null : info.getAllowedValuesInfo();
            if (allowedValues != null && !allowedValues.isConditional()) {
                values.addAll(Arrays.asList(allowedValues.getValue()));
                values.addAll(dataSet(allowedValues.getDataSetKey(), context));
                range = allowedValues.getRange();
            }
        }
        DataValidationHelper helper = workbookSheet.getDataValidationHelper();
        DataValidationConstraint constraint;
        if (!values.isEmpty()) {
            List<String> distinct = new ArrayList<>(new LinkedHashSet<>(values));
            constraint = inlineListFits(distinct)
                    ? helper.createExplicitListConstraint(distinct.toArray(new String[0]))
                    : helper.createFormulaListConstraint(listRange(workbookSheet.getWorkbook(), distinct));
        }
        else if (range != null && range.length == 2) {
            constraint = helper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                    String.valueOf(Math.min(range[0], range[1])), String.valueOf(Math.max(range[0], range[1])));
        }
        else {
            return;
        }
        DataValidation validation = helper.createValidation(constraint, dataCells);
        validation.setShowErrorBox(true);
        validation.setEmptyCellAllowed(true);
        validation.createErrorBox("Invalid value", values.isEmpty()
                ? "Enter a whole number from " + Math.min(range[0], range[1]) + " to " + Math.max(range[0], range[1]) + "."
                : "Choose a value from the list.");
        // XSSF inverts this flag: true shows the in-cell arrow
        validation.setSuppressDropDownArrow(!(workbookSheet instanceof HSSFSheet));
        workbookSheet.addValidationData(validation);
    }

    private static List<String> dataSet(String key, ExcelSheetWriterContext context) {
        if (StringUtils.isEmpty(key) || context.getValidValuesDataSet() == null) {
            return Collections.emptyList();
        }
        List<String> values = context.getValidValuesDataSet().get(key);
        return values == null ? Collections.emptyList() : values;
    }

    private static boolean inlineListFits(List<String> values) {
        int length = values.size() - 1;
        for (String value : values) {
            if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0) {
                return false;
            }
            length += value.length();
        }
        return length <= MAX_INLINE_LIST_LENGTH;
    }

    /** Writes the values as one row of the hidden list sheet and returns an absolute reference to it. */
    private static String listRange(Workbook workbook, List<String> values) {
        org.apache.poi.ss.usermodel.Sheet listSheet = workbook.getSheet(LIST_SHEET);
        if (listSheet == null) {
            listSheet = workbook.createSheet(LIST_SHEET);
            workbook.setSheetHidden(workbook.getSheetIndex(listSheet), true);
        }
        int maxColumns = workbook.getSpreadsheetVersion().getMaxColumns();
        int count = Math.min(values.size(), maxColumns);
        int rowIndex = listSheet.getPhysicalNumberOfRows() == 0 ? 0 : listSheet.getLastRowNum() + 1;
        Row row = listSheet.createRow(rowIndex);
        for (int c = 0; c < count; c++) {
            row.createCell(c).setCellValue(values.get(c));
        }
        String first = new CellReference(rowIndex, 0, true, true).formatAsString();
        String last = new CellReference(rowIndex, count - 1, true, true).formatAsString();
        return "'" + LIST_SHEET + "'!" + first + ":" + last;
    }

    private static void addConditionalFormats(org.apache.poi.ss.usermodel.Sheet workbookSheet, CellRangeAddress range, int firstDataRow, int column, ConditionalFormat[] formats) {
        SheetConditionalFormatting formatting = workbookSheet.getSheetConditionalFormatting();
        String firstCell = new CellReference(firstDataRow, column, false, false).formatAsString();
        for (ConditionalFormat format : formats) {
            ConditionalFormattingRule rule;
            if (StringUtils.isNotEmpty(format.formula())) {
                rule = formatting.createConditionalFormattingRule(format.formula().replace("{cell}", firstCell));
            }
            else {
                String value2 = StringUtils.isNotEmpty(format.value2()) ? format.value2() : null;
                rule = formatting.createConditionalFormattingRule(operator(format.operator()), format.value(), value2);
            }
            if (format.backgroundColor() != IndexedColors.AUTOMATIC) {
                PatternFormatting fill = rule.createPatternFormatting();
                fill.setFillBackgroundColor(format.backgroundColor().getIndex());
                fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
            }
            if (format.fontColor() != IndexedColors.AUTOMATIC || format.bold() || format.italic()) {
                FontFormatting font = rule.createFontFormatting();
                if (format.fontColor() != IndexedColors.AUTOMATIC) {
                    font.setFontColorIndex(format.fontColor().getIndex());
                }
                font.setFontStyle(format.italic(), format.bold());
            }
            formatting.addConditionalFormatting(new CellRangeAddress[] {range}, rule);
        }
    }

    private static byte operator(ConditionalFormat.Operator operator) {
        switch (operator) {
            case NOT_EQUAL: return ComparisonOperator.NOT_EQUAL;
            case GREATER_THAN: return ComparisonOperator.GT;
            case GREATER_OR_EQUAL: return ComparisonOperator.GE;
            case LESS_THAN: return ComparisonOperator.LT;
            case LESS_OR_EQUAL: return ComparisonOperator.LE;
            case BETWEEN: return ComparisonOperator.BETWEEN;
            case NOT_BETWEEN: return ComparisonOperator.NOT_BETWEEN;
            default: return ComparisonOperator.EQUAL;
        }
    }

    /** SXSSF must track columns before rows are flushed for {@link Sheet#autoSizeColumns()}. */
    static void prepareStreamingSheet(SXSSFSheet sheet, Sheet sheetInfo) {
        if (sheetInfo.autoSizeColumns() && !sheet.isColumnTrackedForAutoSizing(0)) {
            sheet.trackAllColumnsForAutoSizing();
        }
    }
}
