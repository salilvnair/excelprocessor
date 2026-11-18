package com.github.salilvnair.excelprocessor.v2.features;

import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.csv.CsvParser;
import com.github.salilvnair.excelprocessor.v2.csv.CsvSheetWriter;
import com.github.salilvnair.excelprocessor.v2.features.sheet.Channel;
import com.github.salilvnair.excelprocessor.v2.features.sheet.OrderSheet;
import com.github.salilvnair.excelprocessor.v2.features.sheet.ReportSheet;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.outline.ColumnType;
import com.github.salilvnair.excelprocessor.v2.outline.OutlineOptions;
import com.github.salilvnair.excelprocessor.v2.outline.SheetOutline;
import com.github.salilvnair.excelprocessor.v2.outline.WorkbookOutline;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** B3 CSV with the same annotations, B2 workbook outline. */
class CsvAndOutlineTest {

    @TempDir
    Path dir;

    @Test
    void csvRoundTripKeepsQuotesCommasLineBreaksAndTypes() throws Exception {
        File file = dir.resolve("orders.csv").toFile();
        try (CsvSheetWriter writer = ExcelSheetWriterFactory.openCsv(new FileOutputStream(file), CsvFormat.csv())) {
            writer.begin(OrderSheet.class);
            OrderSheet a = OrderSheet.of("A-1", "Smith, \"Jo\"\nLtd", "Open", 1234.5);
            a.setPriority(3L);
            a.setChannel(Channel.WEB);
            writer.writeRow(a);
            writer.writeRow(OrderSheet.of("A-2", "=HYPERLINK(\"http://x\")", "Shipped", -2.0));
            assertEquals(2, writer.rowsWritten());
        }
        String text = Files.readString(file.toPath());
        assertTrue(text.startsWith("Order Id,Customer,Status,Amount,Priority,Region,Channel\r\n"), text);
        assertTrue(text.contains("\"Smith, \"\"Jo\"\"\nLtd\""), "embedded quote, comma and line break are quoted");
        assertTrue(text.contains("'=HYPERLINK"), "formula-like text is escaped");
        assertTrue(text.contains(",1234.5,3,,Web shop\r\n"));

        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openCsv(file)) {
            List<OrderSheet> rows = reader.readAll(OrderSheet.class);
            assertEquals(2, rows.size());
            assertEquals("Smith, \"Jo\"\nLtd", rows.get(0).getCustomer());
            assertEquals(1234.5, rows.get(0).getAmount());
            assertEquals(3L, rows.get(0).getPriority());
            assertEquals(Channel.WEB, rows.get(0).getChannel());
            assertEquals("=HYPERLINK(\"http://x\")", rows.get(1).getCustomer(), "the reader removes the escape");
            assertEquals(-2.0, rows.get(1).getAmount());
            assertEquals(3, rows.get(1).getRow(), "row numbers are the line numbers");
        }
    }

    @Test
    void csvValidationAndDynamicHeaders() throws Exception {
        File file = dir.resolve("orders.tsv").toFile();
        Files.writeString(file.toPath(), "\uFEFFOrder Id\tCustomer\tStatus\nA-1\tAcme\tOpen\nA-1\t\tLost\n", StandardCharsets.UTF_8);
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openCsv(file, CsvFormat.tsv(), null)) {
            ValidationReport report = reader.validate(OrderSheet.class);
            assertEquals(2, report.rowsRead());
            assertEquals(3, report.messageCount(), "duplicate id, missing customer, bad status: " + report.messages());
        }
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openCsv(file, CsvFormat.tsv().withInferTypes(true), null)) {
            List<ReportSheet> rows = reader.readAll(ReportSheet.class);
            assertEquals(Arrays.asList("Order Id", "Customer", "Status"), rows.get(0).sheetHeaders());
            assertEquals("Acme", rows.get(0).extract().get("Customer"));
        }
    }

    @Test
    void dynamicRowsAreWrittenInHeaderOrder() throws Exception {
        StringWriter out = new StringWriter();
        try (CsvSheetWriter writer = ExcelSheetWriterFactory.openCsv(out, CsvFormat.csv().withLineSeparator("\n"))) {
            writer.begin(ReportSheet.class, Arrays.asList("B", "A"));
            ReportSheet row = new ReportSheet();
            LinkedHashMap<String, Object> values = new LinkedHashMap<>();
            values.put("A", 1.0);
            values.put("B", LocalDate.of(2026, 1, 2).atStartOfDay());
            row.setDynamicHeaderKeyedCellValueMap(values);
            writer.writeRow(row);
        }
        assertEquals("B,A\n2026-01-02,1\n", out.toString());
    }

    @Test
    void parserHandlesEdgeCases() throws Exception {
        CsvParser parser = new CsvParser(new StringReader("a,\"b\"\"c\",\r\n\r\n\"x\ny\"\rlast"), CsvFormat.csv());
        assertEquals(Arrays.asList("a", "b\"c", ""), parser.next());
        assertEquals(Collections.emptyList(), parser.next(), "blank line");
        assertEquals(Collections.singletonList("x\ny"), parser.next());
        assertEquals(Collections.singletonList("last"), parser.next());
        assertNull(parser.next());
    }

    @Test
    void outlineListsSheetsHeadersCountsAndTypes() throws Exception {
        File file = dir.resolve("outline.xlsx").toFile();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            fillOutlineSheet(wb);
            wb.createSheet("Empty");
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        WorkbookOutline outline = ExcelSheetReaderFactory.outline(file);
        assertEquals(Arrays.asList("Orders", "Empty"), outline.sheetNames());
        assertOrdersOutline(outline.sheet("Orders"));
        assertTrue(outline.sheet("Empty").isEmpty());
        assertEquals(-1, outline.sheet("Empty").headerRow());

        SheetOutline sampled = ExcelSheetReaderFactory.outline(file, OutlineOptions.defaults().sampleRows(1)).sheet("Orders");
        assertEquals(5, sampled.dataRows(), "all rows are counted");
        assertEquals(1, sampled.sampledRows());
        assertEquals(ColumnType.INTEGER, sampled.column("Amount").orElseThrow().type(), "only the first row was sampled");
    }

    @Test
    void outlineReadsXlsAndCsvToo() throws Exception {
        File xls = dir.resolve("outline.xls").toFile();
        try (Workbook wb = new HSSFWorkbook()) {
            fillOutlineSheet(wb);
            try (FileOutputStream out = new FileOutputStream(xls)) {
                wb.write(out);
            }
        }
        assertOrdersOutline(ExcelSheetReaderFactory.outline(xls).sheet("Orders"));

        File csv = dir.resolve("people.csv").toFile();
        Files.writeString(csv.toPath(), "\nId,Zip,Score,Joined,Member\n1,00501,1.5,2024-01-31,true\n2,10001,2,2024-02-01,false\n");
        SheetOutline people = ExcelSheetReaderFactory.outline(csv).sheet("people");
        assertEquals(2, people.headerRow(), "first non-empty row holds the headers");
        assertEquals(2, people.dataRows());
        assertEquals(ColumnType.INTEGER, people.column("Id").orElseThrow().type());
        assertEquals(ColumnType.TEXT, people.column("Zip").orElseThrow().type(), "leading zeros mean text");
        assertEquals(ColumnType.DECIMAL, people.column("Score").orElseThrow().type());
        assertEquals(ColumnType.DATE, people.column("Joined").orElseThrow().type());
        assertEquals(ColumnType.BOOLEAN, people.column("Member").orElseThrow().type());
    }

    private static void fillOutlineSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("Orders");
        Row title = sheet.createRow(0);
        title.createCell(0).setCellValue("Order Id");
        title.createCell(1).setCellValue("Amount");
        title.createCell(2).setCellValue("Placed");
        title.createCell(3).setCellValue("Paid");
        title.createCell(4).setCellValue("Note");
        CellStyle date = wb.createCellStyle();
        date.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
        for (int i = 1; i <= 6; i++) {
            if (i == 4) continue; // empty row is not counted
            Row r = sheet.createRow(i);
            r.createCell(0).setCellValue("A-" + i);
            r.createCell(1).setCellValue(i == 1 ? 10 : i * 1.5);
            r.createCell(2).setCellValue(LocalDate.of(2025, 1, i));
            r.getCell(2).setCellStyle(date);
            r.createCell(3).setCellValue(i % 2 == 0);
        }
    }

    private static void assertOrdersOutline(SheetOutline orders) {
        assertEquals(1, orders.headerRow());
        assertEquals(Arrays.asList("Order Id", "Amount", "Placed", "Paid", "Note"), orders.headers());
        assertEquals(5, orders.dataRows());
        assertEquals(ColumnType.TEXT, orders.column("Order Id").orElseThrow().type());
        assertEquals(ColumnType.DECIMAL, orders.column("Amount").orElseThrow().type());
        assertEquals(ColumnType.DATE, orders.column("Placed").orElseThrow().type());
        assertEquals(ColumnType.BOOLEAN, orders.column("Paid").orElseThrow().type());
        assertEquals(ColumnType.BLANK, orders.column("Note").orElseThrow().type());
        assertEquals("C", orders.column("Placed").orElseThrow().column());
        assertEquals(5, orders.column("Order Id").orElseThrow().nonBlankValues());
    }
}
