package com.github.salilvnair.excelprocessor.v2.reader;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.reader.sheet.ReadSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Templates often pre-fill formulas down many rows that evaluate to "" until data is entered.
 * POI 6 changed DataFormatter to format formula cells by their cached value (POI 5 returned the
 * formula text), which changes whether such rows count as empty. The library pins the behaviour
 * so results are the same on both POI versions: a row whose formulas all evaluate to blank is empty.
 */
class FormulaRowTest {

    @Test
    void rowsWhoseFormulasEvaluateToBlankAreSkippedOnEveryPoiVersion() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Read");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("real row");
            data.createCell(1).setCellValue(30);
            for (int r = 2; r <= 5; r++) {            // template rows: formulas that yield ""
                Row row = sheet.createRow(r);
                row.createCell(0).setCellFormula("IF(B" + (r + 1) + "=\"\",\"\",\"x\")");
            }
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

            List<ReadSheet> rows = ExcelSheetReaderFactory.generate()
                    .read(ReadSheet.class, ExcelSheetContext.builder().workbook(wb).build());
            assertEquals(1, rows.size(), "template formula rows must not be read as data rows");
            assertEquals("real row", rows.get(0).getName());
        }
    }
}
