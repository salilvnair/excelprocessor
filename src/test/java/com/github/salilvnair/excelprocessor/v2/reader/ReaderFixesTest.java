package com.github.salilvnair.excelprocessor.v2.reader;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.ReadDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.ReadSheet;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReaderFixesTest {

    /**
     * Header row: Name | (blank) | Age | (missing cell) | 2024 (numeric). Row 2 has a green fill
     * whose red channel is below 128 and green channel above 127.
     */
    static XSSFWorkbook workbook() {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Read");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("");
        header.createCell(2).setCellValue("Age");
        // column 3 intentionally has no header cell
        header.createCell(4).setCellValue(2024);

        XSSFCellStyle green = wb.createCellStyle();
        green.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        green.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 10, (byte) 200, (byte) 150}, null));

        for (int i = 1; i <= 3; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue("n" + i);
            row.createCell(1).setCellValue("under blank header");
            row.createCell(2).setCellValue(20 + i);
            row.createCell(3).setCellValue("under missing header");
            row.createCell(4).setCellValue(i * 1.5);
            if (i == 1) {
                row.getCell(0).setCellStyle(green);
            }
        }
        return wb;
    }

    @Test
    void blankMissingAndNumericHeaderCellsDoNotCrashTheReader() {
        List<ReadSheet> rows = ExcelSheetReaderFactory.generate()
                .read(ReadSheet.class, ExcelSheetContext.builder().workbook(workbook()).build());
        assertEquals(3, rows.size());
        ReadSheet first = rows.get(0);
        assertEquals("n1", first.getName());
        assertEquals(21L, first.getAge());
        assertEquals(1.5, first.getSales());
    }

    @Test
    void dynamicReaderSkipsBlankHeaderColumns() {
        List<ReadDynamicSheet> rows = ExcelSheetReaderFactory.generate()
                .read(ReadDynamicSheet.class, ExcelSheetContext.builder().workbook(workbook()).build());
        assertEquals(3, rows.size());
        assertEquals(java.util.Arrays.asList("Name", "Age", "2024"), rows.get(0).sheetHeaders());
        assertEquals("n2", rows.get(1).extract().get("Name"));
    }

    @Test
    void foregroundRgbConvertsEveryChannelIndependently() {
        List<ReadSheet> rows = ExcelSheetReaderFactory.generate()
                .read(ReadSheet.class, ExcelSheetContext.builder().workbook(workbook()).build());
        CellInfo nameCell = rows.get(0).cells().get("Name");
        assertArrayEquals(new short[]{10, 200, 150}, nameCell.getForegroundRgb(),
                "green/blue above 127 used to come back negative when red was below 128");
        assertEquals("0AC896", nameCell.getForegroundHexString());
    }

    @Test
    void sheetWithoutHeaderRowReadsAsEmpty() {
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.createSheet("Read");
        List<ReadSheet> rows = ExcelSheetReaderFactory.generate()
                .read(ReadSheet.class, ExcelSheetContext.builder().workbook(wb).build());
        assertTrue(rows.isEmpty());
    }
}
