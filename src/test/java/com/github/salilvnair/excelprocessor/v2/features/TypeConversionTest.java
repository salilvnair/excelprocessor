package com.github.salilvnair.excelprocessor.v2.features;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.features.sheet.Channel;
import com.github.salilvnair.excelprocessor.v2.features.sheet.OrderRecord;
import com.github.salilvnair.excelprocessor.v2.features.sheet.TypedRowSheet;
import com.github.salilvnair.excelprocessor.v2.helper.ValueCoercer;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** A5: java.time, BigDecimal, primitives, enums (by name or @ExcelValue) and records on read. */
class TypeConversionTest {

    @TempDir
    Path dir;

    private File typedFile() throws Exception {
        File file = dir.resolve("typed.xlsx").toFile();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Typed");
            String[] headers = {"Joined", "Created", "Amount", "Quantity", "Active", "Channel", "Code", "Grade", "Due"};
            Row h = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);
            CellStyle date = wb.createCellStyle();
            date.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
            CellStyle dateTime = wb.createCellStyle();
            dateTime.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue(LocalDate.of(2024, 2, 29));
            r.getCell(0).setCellStyle(date);
            r.createCell(1).setCellValue(LocalDateTime.of(2025, 7, 1, 13, 45));
            r.getCell(1).setCellStyle(dateTime);
            r.createCell(2).setCellValue(12.345);
            r.createCell(3).setCellValue(7);
            r.createCell(4).setCellValue("Yes");
            r.createCell(5).setCellValue("Call centre");
            r.createCell(6).setCellValue("00042");
            r.createCell(7).setCellValue("B");
            r.createCell(8).setCellValue("2026-12-31");
            Row r2 = sheet.createRow(2);
            r2.createCell(3).setCellValue("1,200");
            r2.createCell(4).setCellValue(0);
            r2.createCell(5).setCellValue("store");
            r2.createCell(6).setCellValue("not a number");
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        return file;
    }

    private static void assertTypedRows(List<TypedRowSheet> rows) {
        assertEquals(2, rows.size());
        TypedRowSheet first = rows.get(0);
        assertEquals(LocalDate.of(2024, 2, 29), first.getJoined());
        assertEquals(LocalDateTime.of(2025, 7, 1, 13, 45), first.getCreated());
        assertEquals(new BigDecimal("12.345"), first.getAmount());
        assertEquals(7, first.getQuantity());
        assertTrue(first.isActive());
        assertEquals(Channel.PHONE, first.getChannel(), "second @ExcelValue label matches too");
        assertEquals(42L, first.getCode(), "numeric text is parsed into a number field");
        assertEquals('B', first.getGrade());
        assertEquals(LocalDate.of(2026, 12, 31), first.getDue(), "ISO text is parsed into a date field");

        TypedRowSheet second = rows.get(1);
        assertEquals(1200, second.getQuantity(), "thousands separators are accepted");
        assertFalse(second.isActive());
        assertEquals(Channel.STORE, second.getChannel(), "enum names match ignoring case");
        assertNull(second.getCode(), "an unconvertible value leaves the field unset");
        assertNull(second.getJoined());
    }

    @Test
    void listReaderConvertsToFieldTypes() throws Exception {
        List<TypedRowSheet> rows = ExcelSheetReaderFactory.generate()
                .read(TypedRowSheet.class, ExcelSheetContext.builder().excelFile(typedFile()).build());
        assertTypedRows(rows);
    }

    @Test
    void streamingReaderConvertsToFieldTypes() throws Exception {
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(typedFile())) {
            assertTypedRows(reader.readAll(TypedRowSheet.class));
        }
    }

    @Test
    void recordsAreBoundThroughTheirCanonicalConstructor() throws Exception {
        File file = dir.resolve("orders.xlsx").toFile();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Orders");
            String[] headers = {"Order Id", "Customer", "Amount", "Priority", "Channel", "Unmapped"};
            Row h = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue("A-1");
            r.createCell(1).setCellValue("Acme");
            r.createCell(2).setCellValue(99.5);
            r.createCell(3).setCellValue(3);
            r.createCell(4).setCellValue("Web shop");
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("A-2");
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file);
             Stream<OrderRecord> rows = reader.streamRecords(OrderRecord.class)) {
            List<OrderRecord> records = rows.collect(Collectors.toList());
            assertEquals(new OrderRecord("A-1", "Acme", new BigDecimal("99.5"), 3, Channel.WEB), records.get(0));
            assertEquals(new OrderRecord("A-2", null, null, 0, null), records.get(1), "missing values: null, or 0 for primitives");
        }
    }

    @Test
    void coercerCoversCommonTextForms() {
        assertEquals(new BigDecimal("1234.50"), ValueCoercer.coerce("1,234.50", BigDecimal.class, (com.github.salilvnair.excelprocessor.v2.annotation.Cell) null));
        assertEquals(new BigDecimal("0.125"), ValueCoercer.coerce("12.5%", BigDecimal.class, (com.github.salilvnair.excelprocessor.v2.annotation.Cell) null));
        assertEquals(Boolean.FALSE, ValueCoercer.coerce("no", boolean.class, (com.github.salilvnair.excelprocessor.v2.annotation.Cell) null));
        assertEquals(Channel.WEB, ValueCoercer.coerce("web", Channel.class, (com.github.salilvnair.excelprocessor.v2.annotation.Cell) null));
        assertNull(ValueCoercer.coerce("bike", Channel.class, (com.github.salilvnair.excelprocessor.v2.annotation.Cell) null));
        assertEquals("42", ValueCoercer.coerce(42.0, String.class, (com.github.salilvnair.excelprocessor.v2.annotation.Cell) null));
        assertEquals("Web shop", ValueCoercer.enumLabel(Channel.WEB));
        assertEquals(List.of("Web shop", "STORE", "Phone"), ValueCoercer.enumLabels(Channel.class));
    }
}
