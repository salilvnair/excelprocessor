package com.github.salilvnair.excelprocessor.v2.reader;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.ReadDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.ReadSheet;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.TypedReadDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.TypedReadSheet;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.StyledStreamingDynamicSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StreamingReaderTest {

    @TempDir
    Path dir;

    /**
     * Name | Age | Joined (date) | Active (bool) | Total (formula) | 2024 (numeric header) | (blank header) | Ignored-by-class
     * plus an empty row in the middle and a missing cell.
     */
    private File typedFile() throws IOException {
        File file = dir.resolve("typed.xlsx").toFile();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Typed");
            Row h = sheet.createRow(0);
            String[] headers = {"Name", "Age", "Joined", "Active", "Total"};
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);
            h.createCell(5).setCellValue(2024);
            h.createCell(6).setCellValue("");
            h.createCell(7).setCellValue("Extra");
            CellStyle date = wb.createCellStyle();
            date.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
            CellStyle fill = wb.createCellStyle();
            fill.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            fill.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            for (int i = 1; i <= 50; i++) {
                if (i == 10) continue;                      // empty row
                Row r = sheet.createRow(i);
                r.createCell(0).setCellValue("name " + i);
                if (i != 7) r.createCell(1).setCellValue(20 + i); // missing Age on row 7
                Cell d = r.createCell(2);
                d.setCellValue(LocalDate.of(2024, 1, 1).plusDays(i));
                d.setCellStyle(date);
                r.createCell(3).setCellValue(i % 2 == 0);
                r.createCell(4).setCellFormula("B" + (i + 1) + "*2");
                r.createCell(5).setCellValue(i * 100);
                r.createCell(6).setCellValue("under blank header");
                r.createCell(7).setCellValue("extra");
                if (i == 3) r.getCell(0).setCellStyle(fill);
            }
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll(); // cache formula results
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        return file;
    }

    private static <T extends BaseSheet> List<T> inMemory(Class<T> clazz, File file) {
        return ExcelSheetReaderFactory.generate().read(clazz, ExcelSheetContext.builder().excelFile(file).build());
    }

    private static <T extends BaseSheet> List<T> streamed(Class<T> clazz, File file) {
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file)) {
            return reader.readAll(clazz);
        }
    }

    @Test
    void fixedHeaderRowsMatchTheInMemoryReader() throws Exception {
        File file = typedFile();
        List<TypedReadSheet> expected = inMemory(TypedReadSheet.class, file);
        List<TypedReadSheet> actual = streamed(TypedReadSheet.class, file);
        assertEquals(49, expected.size());
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            TypedReadSheet e = expected.get(i), a = actual.get(i);
            String at = "row " + e.getRow();
            assertEquals(e.getRow(), a.getRow(), at);
            assertEquals(e.getName(), a.getName(), at);
            assertEquals(e.getAge(), a.getAge(), at);
            assertEquals(e.getJoined(), a.getJoined(), at);
            assertEquals(e.getActive(), a.getActive(), at);
            assertEquals(e.getTotal(), a.getTotal(), at);
            assertEquals(e.getSales(), a.getSales(), at);
            assertEquals(e.sheetHeaders(), a.sheetHeaders(), at);
            assertEquals(e.cells().keySet(), a.cells().keySet(), at);
        }
        TypedReadSheet r1 = actual.get(0);
        assertEquals("name 1", r1.getName());
        assertEquals(21, r1.getAge());
        assertEquals(LocalDate.of(2024, 1, 2), r1.getJoined().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(Boolean.FALSE, r1.getActive());
        assertEquals(42.0, r1.getTotal());
        assertEquals(100L, r1.getSales());
        assertNull(actual.get(6).getAge(), "missing cell");
        // colours come from the styles table
        CellInfo filled = actual.get(2).cells().get("Name");
        assertEquals(expected.get(2).cells().get("Name").getForegroundHexString(), filled.getForegroundHexString());
        assertNotNull(filled.getForegroundRgb());
    }

    @Test
    void dynamicHeaderRowsMatchTheInMemoryReader() throws Exception {
        File file = typedFile();
        List<TypedReadDynamicSheet> expected = inMemory(TypedReadDynamicSheet.class, file);
        List<TypedReadDynamicSheet> actual = streamed(TypedReadDynamicSheet.class, file);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).extract(), actual.get(i).extract(), "row " + i);
            assertEquals(expected.get(i).sheetHeaders(), actual.get(i).sheetHeaders());
        }
        assertEquals(Arrays.asList("Name", "Age", "Joined", "Active", "Total", "2024", "Extra"), actual.get(0).sheetHeaders());
    }

    @Test
    void readsInlineStringsWrittenByTheStreamingWriter() throws Exception {
        File file = dir.resolve("stream.xlsx").toFile();
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
            writer.beginSheet(StyledStreamingDynamicSheet.class, Arrays.asList("Id", "Name"));
            for (int i = 0; i < 2_000; i++) {
                StyledStreamingDynamicSheet s = new StyledStreamingDynamicSheet();
                LinkedHashMap<String, Object> m = new LinkedHashMap<>();
                m.put("Id", i);
                m.put("Name", "name " + i);
                s.setDynamicHeaderKeyedCellValueMap(m);
                writer.writeRow(s);
            }
            writer.finish(file.toPath());
        }
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file,
                ExcelSheetContext.builder().sheetName("DynamicStream").build());
             Stream<ReadDynamicSheet> rows = reader.stream(ReadDynamicSheet.class)) {
            Iterator<ReadDynamicSheet> it = rows.iterator();
            Map<String, Object> first = it.next().extract();
            assertEquals(0.0, first.get("Id"));
            assertEquals("name 0", first.get("Name"));
            long remaining = 0;
            while (it.hasNext()) { it.next(); remaining++; }
            assertEquals(1_999, remaining);
        }
    }

    @Test
    void handlesBlankAndNumericHeadersLikeTheFixedReader() {
        File file = dir.resolve("read.xlsx").toFile();
        try (XSSFWorkbook wb = ReaderFixesTest.workbook(); FileOutputStream out = new FileOutputStream(file)) {
            wb.write(out);
        } catch (IOException e) { throw new RuntimeException(e); }
        List<ReadSheet> rows = streamed(ReadSheet.class, file);
        assertEquals(3, rows.size());
        assertEquals("n1", rows.get(0).getName());
        assertEquals(21L, rows.get(0).getAge());
        assertEquals(1.5, rows.get(0).getSales());
        assertArrayEquals(new short[]{10, 200, 150}, rows.get(0).cells().get("Name").getForegroundRgb());
    }

    @Test
    void missingSheetAndUnsupportedLayoutsFailClearly() throws Exception {
        File file = typedFile();
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file)) {
            assertThrows(RuntimeException.class, () -> reader.stream(ReadSheet.class)); // sheet "Read" absent
        }
    }
}
