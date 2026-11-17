package com.github.salilvnair.excelprocessor.v2.writer;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.*;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.DefaultTempFileCreationStrategy;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StreamingWriterTest {

    @TempDir
    Path poiTempDir;

    @BeforeEach
    void captureTempFiles() {
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy(poiTempDir.toFile()));
    }

    @AfterEach
    void restoreTempFiles() {
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy());
    }

    private long poiTempFileCount() throws IOException {
        try (Stream<Path> files = Files.walk(poiTempDir)) {
            return files.filter(Files::isRegularFile).count();
        }
    }

    // ---------------------------------------------------------------- writer selection

    @Test
    void factoryRoutesEveryHorizontalAnnotationCombination() {
        assertInstanceOf(StreamingDynamicHorizontalSheetWriter.class, ExcelSheetFactory.generateWriter(StyledStreamingDynamicSheet.class));
        assertInstanceOf(DynamicHorizontalSheetWriter.class, ExcelSheetFactory.generateWriter(StyledDynamicSheet.class));
        assertInstanceOf(StreamingHorizontalSheetWriter.class, ExcelSheetFactory.generateWriter(StreamingPersonSheet.class));
        assertInstanceOf(HorizontalSheetWriter.class, ExcelSheetFactory.generateWriter(PersonSheet.class));
    }

    // ---------------------------------------------------------------- list based streaming

    @Test
    void listBasedDynamicStreamingWriteToOutputStream() throws Exception {
        List<StyledStreamingDynamicSheet> rows = new ArrayList<>();
        for (int i = 0; i < 5_000; i++) {
            StyledStreamingDynamicSheet s = new StyledStreamingDynamicSheet();
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("Id", (double) i);
            m.put("Name", "n" + i);
            s.setDynamicHeaderKeyedCellValueMap(m);
            rows.add(s);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelSheetWriterFactory.generate().write(rows, ExcelSheetContext.builder().build(), out);
        assertEquals(0, poiTempFileCount(), "SXSSF temp files must be released after write");
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheet("DynamicStream");
            assertEquals(5_000, sheet.getLastRowNum());
            assertEquals("Id", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals(4_999.0, sheet.getRow(5_000).getCell(0).getNumericCellValue());
            assertEquals("n4999", sheet.getRow(5_000).getCell(1).getStringCellValue());
            assertEquals(BorderStyle.THIN, sheet.getRow(5_000).getCell(1).getCellStyle().getBorderTop());
            assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        }
    }

    @Test
    void workbookApiReturnsStreamingWorkbookThatCallerDisposes() throws Exception {
        List<StreamingPersonSheet> rows = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            StreamingPersonSheet p = new StreamingPersonSheet();
            p.setName("n" + i);
            p.setAge((long) i);
            rows.add(p);
        }
        Workbook wb = ExcelSheetWriterFactory.generate().workbook(rows, ExcelSheetContext.builder().build());
        try {
            assertInstanceOf(SXSSFWorkbook.class, wb);
            assertTrue(poiTempFileCount() > 0, "streaming workbook should be backed by temp files");
            wb.write(OutputStream.nullOutputStream());
        }
        finally {
            ExcelSheetWriter.dispose(wb);
            ExcelSheetWriter.dispose(wb); // idempotent
        }
        assertEquals(0, poiTempFileCount());
    }

    @Test
    void streamingSheetKeepsPreviousSheetsOfAMultiSheetWrite() {
        Map<String, List<? extends BaseSheet>> sheets = new LinkedHashMap<>();
        sheets.put("People", Collections.singletonList(person("plain", 1)));
        StreamingPersonSheet streamed = new StreamingPersonSheet();
        streamed.setName("streamed");
        streamed.setAge(2L);
        sheets.put("PeopleStream", Collections.singletonList(streamed));
        Workbook wb = ExcelSheetWriterFactory.generate().workbook(sheets, ExcelSheetContext.builder().build());
        try {
            assertNotNull(wb.getSheet("People"), "first sheet lost when the second sheet streams");
            assertNotNull(wb.getSheet("PeopleStream"));
        }
        finally {
            ExcelSheetWriter.dispose(wb);
        }
    }

    // ---------------------------------------------------------------- row-at-a-time API

    @Test
    void rowStreamingDynamicSheetFromLazySource() throws Exception {
        int total = 20_000;
        int[] produced = {0};
        StreamingExcelSheetWriter.RowSource<StyledStreamingDynamicSheet> source = () -> {
            if (produced[0] == total) {
                return null;
            }
            int i = produced[0]++;
            StyledStreamingDynamicSheet s = new StyledStreamingDynamicSheet();
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("Id", i);                                 // Integer
            m.put("Active", i % 2 == 0);                    // Boolean
            m.put("Joined", LocalDate.of(2024, 1, 1).plusDays(i % 365));
            if (i % 3 != 0) {
                m.put("Note", "note " + i);                 // missing for every third row
            }
            m.put("Unknown", "ignored, not in header list");
            s.setDynamicHeaderKeyedCellValueMap(m);
            return s;
        };
        ExcelSheetContext context = ExcelSheetContext.builder().build();
        Path out = poiTempDir.resolve("out").resolve("dynamic.xlsx");
        Files.createDirectories(out.getParent());
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(context)) {
            writer.beginSheet(StyledStreamingDynamicSheet.class, Arrays.asList("Id", "Active", "Joined", "Note"));
            writer.writeRows(source);
            assertEquals(total, writer.rowsWritten());
            ExcelSheetWriterContext writerContext = context.writerContext();
            // nothing but the header prototype is retained
            assertEquals(1, writerContext.getSheetData().size());
            assertNull(writerContext.getSheetDataObj());
            writer.finish(out);
        }
        try (Stream<Path> files = Files.walk(poiTempDir)) {
            assertEquals(1, files.filter(Files::isRegularFile).count(), "only the output file may remain");
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(out))) {
            Sheet sheet = wb.getSheet("DynamicStream");
            assertEquals(total, sheet.getLastRowNum());
            Row header = sheet.getRow(0);
            assertEquals(Arrays.asList("Id", "Active", "Joined", "Note"),
                    Arrays.asList(header.getCell(0).getStringCellValue(), header.getCell(1).getStringCellValue(),
                            header.getCell(2).getStringCellValue(), header.getCell(3).getStringCellValue()));
            assertNull(header.getCell(4));
            Row r = sheet.getRow(5);                         // i = 4
            assertEquals(4.0, r.getCell(0).getNumericCellValue());
            assertTrue(r.getCell(1).getBooleanCellValue());
            assertEquals(LocalDate.of(2024, 1, 5), r.getCell(2).getLocalDateTimeCellValue().toLocalDate());
            assertEquals("note 4", r.getCell(3).getStringCellValue());
            Cell missing = sheet.getRow(4).getCell(3);      // i = 3 -> no Note
            assertEquals(CellType.BLANK, missing.getCellType());
            assertEquals(BorderStyle.THIN, missing.getCellStyle().getBorderTop());
            assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        }
    }

    @Test
    void rowStreamingFixedAndDynamicSheetsInOneWorkbook() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
            writer.beginSheet(PersonSheet.class);
            for (int i = 0; i < 1_000; i++) {
                writer.writeRow(person("p" + i, i));
            }
            writer.beginSheet(StyledStreamingDynamicSheet.class, Collections.singletonList("Name"));
            StyledStreamingDynamicSheet row = new StyledStreamingDynamicSheet();
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("Name", "dyn");
            row.setDynamicHeaderKeyedCellValueMap(m);
            writer.writeRow(row);
            assertEquals(1_001, writer.totalRowsWritten());
            writer.finish(out);
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet people = wb.getSheet("People");
            assertEquals(1_000, people.getLastRowNum());
            int ageCol = StyleCacheTest.column(people, "Age");
            assertEquals(999.0, people.getRow(1_000).getCell(ageCol).getNumericCellValue());
            assertEquals("0", people.getRow(1_000).getCell(ageCol).getCellStyle().getDataFormatString());
            assertEquals("dyn", wb.getSheet("DynamicStream").getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    void rowStreamingReleasesTempFilesWhenSourceFails() throws Exception {
        int[] produced = {0};
        StreamingExcelSheetWriter.RowSource<PersonSheet> failing = () -> {
            if (produced[0]++ == 500) {
                throw new IllegalStateException("source broke");
            }
            return person("p", produced[0]);
        };
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
                writer.beginSheet(PersonSheet.class);
                writer.writeRows(failing);
            }
        });
        assertEquals("source broke", e.getMessage());
        assertEquals(0, poiTempFileCount());
    }

    @Test
    void listWriteReleasesTempFilesWhenSerializationFails() throws Exception {
        List<StreamingPersonSheet> rows = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            StreamingPersonSheet p = new StreamingPersonSheet();
            p.setName("n" + i);
            rows.add(p);
        }
        OutputStream broken = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("disk full");
            }
        };
        assertThrows(IOException.class, () -> ExcelSheetWriterFactory.generate().write(rows, ExcelSheetContext.builder().build(), broken));
        assertEquals(0, poiTempFileCount());
    }

    @Test
    void rowStreamingRejectsMisuse() {
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
            assertThrows(IllegalStateException.class, () -> writer.writeRow(person("x", 1)));
            writer.beginSheet(PersonSheet.class);
            assertThrows(RuntimeException.class, () -> writer.writeRow(new StyledStreamingDynamicSheet()));
            assertThrows(RuntimeException.class, () -> writer.beginSheet(StyledStreamingDynamicSheet.class, Arrays.asList("A", "A")));
        }
    }

    static PersonSheet person(String name, long age) {
        PersonSheet p = new PersonSheet();
        p.setName(name);
        p.setAge(age);
        return p;
    }
}
