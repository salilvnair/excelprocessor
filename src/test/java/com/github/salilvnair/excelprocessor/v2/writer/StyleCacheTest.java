package com.github.salilvnair.excelprocessor.v2.writer;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.LinkSheet;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.PersonSheet;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.TaskStyledDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.StyledDynamicSheet;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StyleCacheTest {

    static List<StyledDynamicSheet> dynamicRows(int n) {
        List<StyledDynamicSheet> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            StyledDynamicSheet s = new StyledDynamicSheet();
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("Id", (double) i);
            m.put("Name", "name-" + i);
            m.put("City", i % 2 == 0 ? "Pune" : null);
            s.setDynamicHeaderKeyedCellValueMap(m);
            rows.add(s);
        }
        return rows;
    }

    @Test
    void dynamicCellsShareStylesInsteadOfOnePerCell() {
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        Workbook wb = writer.workbook(dynamicRows(2_000), ExcelSheetContext.builder().build());
        assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("Dynamic");
        assertEquals(2_000, sheet.getLastRowNum());
        Cell c = sheet.getRow(1999).getCell(1);
        assertEquals("name-1998", c.getStringCellValue());
        assertEquals(BorderStyle.THIN, c.getCellStyle().getBorderTop());
        // every body cell shares the same style object index
        assertEquals(sheet.getRow(1).getCell(0).getCellStyle().getIndex(), sheet.getRow(1500).getCell(2).getCellStyle().getIndex());
    }

    @Test
    void fixedFieldsKeepDistinctStylesAndShareEquivalentOnes() {
        List<PersonSheet> rows = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            PersonSheet p = new PersonSheet();
            p.setName("n" + i);
            p.setAge((long) i);
            p.setWebsite("https://example.com/" + i);
            rows.add(p);
        }
        Workbook wb = ExcelSheetWriterFactory.generate().workbook(rows, ExcelSheetContext.builder().build());
        assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("People");
        int nameCol = -1, ageCol = -1;
        Row header = sheet.getRow(0);
        for (Cell h : header) {
            if ("Name".equals(h.getStringCellValue())) nameCol = h.getColumnIndex();
            if ("Age".equals(h.getStringCellValue())) ageCol = h.getColumnIndex();
        }
        CellStyle nameStyle = sheet.getRow(10).getCell(nameCol).getCellStyle();
        CellStyle ageStyle = sheet.getRow(10).getCell(ageCol).getCellStyle();
        // Name: border only, no fill. Age: yellow fill, bold, "0" format.
        assertEquals(BorderStyle.THIN, nameStyle.getBorderTop());
        assertEquals(FillPatternType.NO_FILL, nameStyle.getFillPattern());
        assertEquals(FillPatternType.SOLID_FOREGROUND, ageStyle.getFillPattern());
        assertEquals(IndexedColors.YELLOW.getIndex(), ageStyle.getFillForegroundColor());
        assertTrue(wb.getFontAt(ageStyle.getFontIndex()).getBold());
        assertEquals("0", ageStyle.getDataFormatString());
        assertNotEquals(nameStyle.getIndex(), ageStyle.getIndex());
        assertEquals(nameStyle.getIndex(), sheet.getRow(900).getCell(nameCol).getCellStyle().getIndex());
        assertEquals(ageStyle.getIndex(), sheet.getRow(900).getCell(ageCol).getCellStyle().getIndex());
        assertEquals(999.0, sheet.getRow(1000).getCell(ageCol).getNumericCellValue());
    }

    @Test
    void hyperlinkCellsReuseOneStyleAndOneFont() {
        List<LinkSheet> rows = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            LinkSheet l = new LinkSheet();
            l.setLink("https://example.com/" + i);
            l.setLinks("https://a.example.com/" + i + " https://b.example.com/" + i);
            l.setFlag(i % 2 == 0);
            rows.add(l);
        }
        Workbook wb = ExcelSheetWriterFactory.generate().workbook(rows, ExcelSheetContext.builder().build());
        assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        assertTrue(wb.getNumberOfFonts() < 5, "fonts=" + wb.getNumberOfFonts());
        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("Links");
        int linkCol = column(sheet, "Link"), flagCol = column(sheet, "Flag");
        Cell link = sheet.getRow(500).getCell(linkCol);
        assertEquals("https://example.com/499", link.getHyperlink().getAddress());
        Font font = wb.getFontAt(link.getCellStyle().getFontIndex());
        assertTrue(font.getBold());
        assertEquals(Font.U_SINGLE, font.getUnderline());
        // booleans used to be silently dropped
        assertEquals(CellType.BOOLEAN, sheet.getRow(1).getCell(flagCol).getCellType());
        assertTrue(sheet.getRow(1).getCell(flagCol).getBooleanCellValue());
    }

    @Test
    void customTaskMutatingCellStyleOnlyAffectsThatCellAndIsDeduplicated() {
        List<TaskStyledDynamicSheet> rows = new ArrayList<>();
        for (int i = 0; i < 2_000; i++) {
            TaskStyledDynamicSheet s = new TaskStyledDynamicSheet();
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("Id", (double) i);
            m.put("City", i % 2 == 0 ? "Pune" : null);
            s.setDynamicHeaderKeyedCellValueMap(m);
            rows.add(s);
        }
        Workbook wb = ExcelSheetWriterFactory.generate().workbook(rows, ExcelSheetContext.builder().build());
        assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("TaskStyled");
        CellStyle filled = sheet.getRow(1).getCell(1).getCellStyle();   // i=0 -> "Pune"
        CellStyle empty = sheet.getRow(2).getCell(1).getCellStyle();    // i=1 -> null
        assertEquals(FillPatternType.NO_FILL, filled.getFillPattern());
        assertEquals(FillPatternType.SOLID_FOREGROUND, empty.getFillPattern());
        assertEquals(IndexedColors.YELLOW.getIndex(), empty.getFillForegroundColor());
        // default styles applied before the task are kept
        assertEquals(BorderStyle.THIN, empty.getBorderTop());
        assertEquals(BorderStyle.THIN, filled.getBorderTop());
        // the Id column never got the highlight
        assertEquals(FillPatternType.NO_FILL, sheet.getRow(2).getCell(0).getCellStyle().getFillPattern());
        // all empty cells share one style
        assertEquals(empty.getIndex(), sheet.getRow(1500).getCell(1).getCellStyle().getIndex());
    }

    static int column(org.apache.poi.ss.usermodel.Sheet sheet, String header) {
        for (Cell h : sheet.getRow(0)) {
            if (header.equals(h.getStringCellValue())) {
                return h.getColumnIndex();
            }
        }
        throw new AssertionError("no header " + header);
    }
}
