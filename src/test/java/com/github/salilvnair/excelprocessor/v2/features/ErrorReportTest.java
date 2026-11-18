package com.github.salilvnair.excelprocessor.v2.features;

import com.github.salilvnair.excelprocessor.v2.features.sheet.OrderSheet;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.report.ExcelErrorReport;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** A2: messages are written back into a copy of the upload. */
class ErrorReportTest {

    @TempDir
    Path dir;

    @Test
    void highlightsCommentsAndListsEveryMessage() throws Exception {
        File upload = StreamingValidationTest.ordersFile(dir);
        byte[] before = Files.readAllBytes(upload.toPath());
        ValidationReport report;
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(upload)) {
            report = reader.validate(OrderSheet.class);
        }
        Path out = dir.resolve("errors.xlsx");
        ExcelErrorReport.of(upload, report).highlight(IndexedColors.ROSE).writeTo(out);

        assertArrayEquals(before, Files.readAllBytes(upload.toPath()), "the upload itself is untouched");
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(out.toFile()))) {
            Sheet orders = wb.getSheet("Orders");
            Cell missingCustomer = orders.getRow(2).getCell(1);
            assertEquals(IndexedColors.ROSE.getIndex(), missingCustomer.getCellStyle().getFillForegroundColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, missingCustomer.getCellStyle().getFillPattern());
            assertNotNull(missingCustomer.getCellComment());
            assertTrue(missingCustomer.getCellComment().getString().getString().toLowerCase().contains("customer"));

            Cell valid = orders.getRow(1).getCell(1);
            assertNull(valid.getCellComment());
            assertNotEquals(IndexedColors.ROSE.getIndex(), valid.getCellStyle().getFillForegroundColor());

            Sheet errors = wb.getSheet("Errors");
            assertNotNull(errors);
            assertEquals(report.messages().size(), errors.getLastRowNum(), "one row per message below the header");
            assertEquals("Sheet", errors.getRow(0).getCell(0).getStringCellValue());
            Cell link = errors.getRow(1).getCell(1);
            assertEquals(HyperlinkType.DOCUMENT, link.getHyperlink().getType());
            assertTrue(link.getHyperlink().getAddress().startsWith("'Orders'!"));
            assertEquals(wb.getSheetIndex(errors), wb.getActiveSheetIndex());

            // highlighted cells share one style per original style
            int styles = wb.getNumCellStyles();
            assertTrue(styles <= 8, "style count stays small, was " + styles);
        }
    }

    @Test
    void summarySheetNameDoesNotClash() throws Exception {
        File upload = StreamingValidationTest.ordersFile(dir);
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(upload))) {
            wb.createSheet("Errors");
            ValidationReport report;
            try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(upload)) {
                report = reader.validate(OrderSheet.class);
            }
            Workbook annotated = ExcelErrorReport.of(wb, report.messages()).comments(false).annotate();
            assertNotNull(annotated.getSheet("Errors (2)"));
            assertNull(annotated.getSheet("Orders").getRow(2).getCell(1).getCellComment());
        }
    }
}
