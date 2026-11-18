package com.github.salilvnair.excelprocessor.v2.features;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.features.sheet.OrderSheet;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** A1: @CellValidation rules run per row while streaming; rows are never collected. */
class StreamingValidationTest {

    @TempDir
    Path dir;

    /**
     * Row 2 valid, row 3 missing customer, row 4 bad status, row 5 duplicates row 2's id,
     * row 6 valid, row 7 duplicates again.
     */
    static File ordersFile(Path dir) throws Exception {
        File file = dir.resolve("orders.xlsx").toFile();
        Object[][] data = {
                {"A-1", "Acme", "Open"},
                {"A-2", null, "Shipped"},
                {"A-3", "Bolt", "Lost"},
                {"A-1", "Crane", "Closed"},
                {"A-4", "Delta", "Open"},
                {"A-1", "Echo", "Open"},
        };
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Orders");
            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("Order Id");
            h.createCell(1).setCellValue("Customer");
            h.createCell(2).setCellValue("Status");
            for (int i = 0; i < data.length; i++) {
                Row r = sheet.createRow(i + 1);
                for (int c = 0; c < data[i].length; c++) {
                    if (data[i][c] != null) r.createCell(c).setCellValue((String) data[i][c]);
                }
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        return file;
    }

    @Test
    void validatesEachRowAndReportsCells() throws Exception {
        List<String> savedIds = new ArrayList<>();
        ValidationReport report;
        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(ordersFile(dir))) {
            report = reader.validate(OrderSheet.class, (row, messages) -> {
                if (messages.isEmpty()) {
                    savedIds.add(row.getOrderId());
                }
            });
        }
        List<String> found = report.messages().stream()
                .map(m -> m.cellReference() + ":" + m.getValidatorType())
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("A5:UNIQUE", "A7:UNIQUE", "B3:REQUIRED", "C4:ALLOWED_VALUES"), found);
        assertEquals(6, report.rowsRead());
        assertEquals(4, report.rowsWithErrors());
        assertEquals(List.of("A-1", "A-4"), savedIds, "valid rows reach the handler with no messages");
        CellValidationMessage required = report.messages().stream().filter(m -> m.getValidatorType() == ValidatorType.REQUIRED).findFirst().orElseThrow();
        assertEquals("Orders", required.getSheet());
        assertEquals("Customer", required.getHeader());
    }

    @Test
    void listValidationReportsEveryDuplicateButNotUniqueValues() throws Exception {
        ExcelSheetContext context = ExcelSheetContext.builder().excelFile(ordersFile(dir)).build();
        var reader = ExcelSheetReaderFactory.generate();
        List<OrderSheet> rows = reader.read(OrderSheet.class, context);
        List<CellValidationMessage> messages = reader.validate(rows, context);
        List<String> unique = messages.stream()
                .filter(m -> m.getValidatorType() == ValidatorType.UNIQUE)
                .map(CellValidationMessage::cellReference)
                .sorted()
                .collect(Collectors.toList());
        // 4.0.x compared each row with itself, so every non-empty id was reported as a duplicate
        assertEquals(List.of("A2", "A5", "A7"), unique);
    }
}
