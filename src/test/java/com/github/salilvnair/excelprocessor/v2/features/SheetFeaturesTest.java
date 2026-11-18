package com.github.salilvnair.excelprocessor.v2.features;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.features.sheet.Channel;
import com.github.salilvnair.excelprocessor.v2.features.sheet.OrderSheet;
import com.github.salilvnair.excelprocessor.v2.features.sheet.ReportSheet;
import com.github.salilvnair.excelprocessor.v2.model.AllowedValuesInfo;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/** A3 dropdowns, A4 freeze/filter/auto width/header height, B1 native conditional formatting. */
class SheetFeaturesTest {

    @TempDir
    Path dir;

    private static ExcelSheetContext context() {
        Map<String, List<String>> dataSet = new HashMap<>();
        // long enough (and with a comma) to need the hidden list sheet
        List<String> regions = IntStream.rangeClosed(1, 60).mapToObj(i -> "Region number " + i).collect(Collectors.toList());
        regions.add("North, East");
        dataSet.put("regions", regions);
        return ExcelSheetContext.builder().validValuesDataSet(dataSet).build();
    }

    private static List<OrderSheet> rows() {
        List<OrderSheet> rows = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            OrderSheet row = OrderSheet.of("A-" + i, "Customer with a fairly long name " + i, "Open", i * 100.0 - 500);
            row.setChannel(Channel.values()[i % 3]);
            rows.add(row);
        }
        return rows;
    }

    private static XSSFWorkbook reopen(byte[] bytes) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    private static void assertFeatures(XSSFWorkbook wb, int lastDataRow) {
        XSSFSheet sheet = wb.getSheet("Orders");
        // A4 freeze header
        assertNotNull(sheet.getPaneInformation());
        assertTrue(sheet.getPaneInformation().isFreezePane());
        assertEquals(1, sheet.getPaneInformation().getHorizontalSplitPosition());
        // A4 auto filter over header + written rows
        assertEquals("A1:G" + (lastDataRow + 1), sheet.getCTWorksheet().getAutoFilter().getRef());
        // A4 header height and auto width
        assertEquals(24f, sheet.getRow(0).getHeightInPoints(), 0.01);
        assertTrue(sheet.getColumnWidth(1) > sheet.getDefaultColumnWidth() * 256 * 2, "Customer column was auto sized");

        // A3 dropdowns
        Map<String, String> constraints = new HashMap<>();
        for (DataValidation validation : sheet.getDataValidations()) {
            CellRangeAddress range = validation.getRegions().getCellRangeAddress(0);
            assertEquals(1, range.getFirstRow(), "validation starts on the first data row");
            assertEquals(wb.getSpreadsheetVersion().getLastRowIndex(), range.getLastRow(), "and covers the whole column");
            DataValidationConstraint c = validation.getValidationConstraint();
            String text = c.getExplicitListValues() != null ? String.join("|", c.getExplicitListValues())
                    : c.getFormula1() + (c.getFormula2() == null ? "" : ".." + c.getFormula2());
            constraints.put(org.apache.poi.ss.util.CellReference.convertNumToColString(range.getFirstColumn()), text);
        }
        assertEquals("Open|Shipped|Closed", constraints.get("C"));
        assertEquals("1..10", constraints.get("E"), "int range becomes a whole-number limit");
        assertEquals("'ExcelProcessorLists'!$A$1:$BI$1", constraints.get("F"), "long list goes to the hidden sheet");
        assertEquals("Web shop|STORE|Phone", constraints.get("G"), "enum fields offer their labels");
        Sheet lists = wb.getSheet("ExcelProcessorLists");
        assertTrue(wb.isSheetHidden(wb.getSheetIndex(lists)));
        assertEquals("North, East", lists.getRow(0).getCell(60).getStringCellValue());

        // B1 conditional formatting on Amount (column D)
        SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
        assertEquals(2, formatting.getNumConditionalFormattings());
        ConditionalFormatting negative = formatting.getConditionalFormattingAt(0);
        assertEquals("D2:D1048576", negative.getFormattingRanges()[0].formatAsString());
        ConditionalFormattingRule rule = negative.getRule(0);
        assertEquals(ComparisonOperator.LT, rule.getComparisonOperation());
        assertEquals("0", rule.getFormula1());
        assertEquals(IndexedColors.RED.getIndex(), rule.getFontFormatting().getFontColorIndex());
        assertTrue(rule.getFontFormatting().isBold());
        assertEquals(IndexedColors.LIGHT_GREEN.getIndex(), formatting.getConditionalFormattingAt(1).getRule(0).getPatternFormatting().getFillBackgroundColor());
    }

    @Test
    void listWriterAppliesSheetFeatures() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelSheetWriterFactory.generate().write(rows(), context(), out);
        try (XSSFWorkbook wb = reopen(out.toByteArray())) {
            assertFeatures(wb, 30);
            // enum values are written with their @ExcelValue label, else their name
            assertEquals("STORE", wb.getSheet("Orders").getRow(1).getCell(6).getStringCellValue());
            assertEquals("Web shop", wb.getSheet("Orders").getRow(3).getCell(6).getStringCellValue());
        }
    }

    @Test
    void streamingWriterAppliesSheetFeatures() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(context())) {
            writer.beginSheet(OrderSheet.class);
            rows().forEach(writer::writeRow);
            writer.finish(out);
        }
        try (XSSFWorkbook wb = reopen(out.toByteArray())) {
            assertFeatures(wb, 30);
        }
    }

    @Test
    void blankTemplateStillOffersDropdowns() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(context())) {
            writer.beginSheet(OrderSheet.class);
            writer.finish(out);
        }
        try (XSSFWorkbook wb = reopen(out.toByteArray())) {
            XSSFSheet sheet = wb.getSheet("Orders");
            assertEquals(4, sheet.getDataValidations().size());
            assertEquals("A1:G1", sheet.getCTWorksheet().getAutoFilter().getRef());
        }
    }

    @Test
    void dynamicHeadersTakeDropdownsFromValidationInfo() throws Exception {
        Map<String, CellValidationInfo> info = new HashMap<>();
        info.put("Grade", CellValidationInfo.builder().allowedValuesInfo(AllowedValuesInfo.builder().value(new String[] {"A", "B", "C"}).build()).build());
        ExcelSheetContext context = ExcelSheetContext.builder().headerKeyedCellValidationInfo(info).build();
        ReportSheet row = new ReportSheet();
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("Student", "Ann");
        values.put("Grade", "A");
        row.setDynamicHeaderKeyedCellValueMap(values);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelSheetWriterFactory.generate().write(Collections.singletonList(row), context, out);
        try (XSSFWorkbook wb = reopen(out.toByteArray())) {
            XSSFSheet sheet = wb.getSheet("Report");
            assertEquals(1, sheet.getDataValidations().size());
            DataValidation validation = sheet.getDataValidations().get(0);
            assertEquals(1, validation.getRegions().getCellRangeAddress(0).getFirstColumn());
            assertArrayEquals(new String[] {"A", "B", "C"}, validation.getValidationConstraint().getExplicitListValues());
            assertTrue(sheet.getPaneInformation().isFreezePane());
        }
    }
}
