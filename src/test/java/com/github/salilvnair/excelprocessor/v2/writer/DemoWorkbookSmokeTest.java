package com.github.salilvnair.excelprocessor.v2.writer;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.*;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the writer demos shipped in src/main (horizontal, vertical, dynamic, multi-oriented, with and
 * without templates) and dumps every cell's value and visual style to
 * target/demo-workbook-snapshot.txt, so output can be diffed between library versions.
 */
class DemoWorkbookSmokeTest {

    private static final String TEMPLATES = "src/test/resources/excel/template/";

    private static ExcelSheetContext ctx() {
        return ExcelSheetContext.builder().build();
    }

    private static ExcelSheetContext template(String name) {
        return ExcelSheetContext.builder().template(new File(TEMPLATES + name)).build();
    }

    @Test
    void demoWorkbooksBuildAndSnapshot() throws Exception {
        ExcelSheetWriter w = ExcelSheetWriterFactory.generate();
        Map<String, Callable<Workbook>> demos = new LinkedHashMap<>();
        demos.put("NoTemplateHorizontalSheet", () -> w.workbook(CompanyInfoNoTemplateHorizontalSheetMock.generateHorizontalSheets(), ctx()));
        demos.put("NoTemplateHorizontalDynamicSheet", () -> w.workbook(CompanyInfoNoTemplateHorizontalSheetMock.generateHorizontalDynamicSheets(), ctx()));
        demos.put("NoTemplateHorizontalSheetNumberFormat", () -> w.workbook(UserInfoNoTemplateHorizontalSheetMock.generateHorizontalDynamicSheets(), ctx()));
        demos.put("NoTemplateVerticalSheet", () -> w.workbook(CompanyInfoNoTemplateVerticalSheetMock.generateVerticalSheets(), ctx()));
        demos.put("NoTemplateVerticalDynamicSheet", () -> w.workbook(CompanyInfoNoTemplateVerticalSheetMock.generateVerticalDynamicSheets(), ctx()));
        demos.put("MultiOriented1", () -> w.workbook(ctx(), CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock.generateMultiOrientedHorizontalSheets()));
        demos.put("MultiOriented2", () -> w.workbook(ctx(), CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock.generateMultiOrientedHorizontalDynamicSheets()));
        demos.put("MultiOriented3", () -> w.workbook(ctx(), List.<List<? extends BaseSheet>>of(CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock.generateMultiOrientedCompanyInfoHorizontalSheets(), CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedUserInfoVerticalSheets())));
        demos.put("MultiOriented4", () -> w.workbook(ctx(), List.<List<? extends BaseSheet>>of(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedCompanyInfoVerticalSheets(), CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedUserInfoVerticalSheets())));
        demos.put("MultiOriented5", () -> w.workbook(ctx(), List.<List<? extends BaseSheet>>of(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedCompanyInfoVerticalDynamicSheets(), CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedUserInfoVerticalDynamicSheets())));
        demos.put("MultiOriented6", () -> w.workbook(ctx(), List.<List<? extends BaseSheet>>of(CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock.generateMultiOrientedCompanyInfoHorizontalDynamicSheets(), CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedUserInfoVerticalDynamicSheets())));
        demos.put("RandomPosition1", () -> w.workbook(ctx(), CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock.generateMultiOrientedHorizontalSheets()));
        demos.put("RandomPosition2", () -> w.workbook(ctx(), CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionVerticalSheetMock.generateMultiOrientedVerticalSheets()));
        demos.put("RandomPosition6", () -> w.workbook(ctx(), List.<List<? extends BaseSheet>>of(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock.generateMultiOrientedCompanyInfoHorizontalDynamicSheets(), CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock.generateMultiOrientedUserInfoHorizontalDynamicSheets())));
        demos.put("WithTemplateHorizontalSheet", () -> w.workbook(CompanyInfoWithTemplateHorizontalSheetMock.generateHorizontalSheets(), template("TemplateHorizontalSheetTest.xlsx")));
        demos.put("WithTemplateMultiHeaderIndex", () -> w.workbook(MultipleHeaderIndexWithTemplateHorizontalSheetMock.generateHorizontalSheets(), template("TemplateMultiHeaderIndexHorizontalSheetTest.xlsx")));
        demos.put("WithTemplateMultiOriented1", () -> w.workbook(template("TemplateMultiOrientedTest1.xlsx"), CompanyInfoAndUserInfoMultiOrientedWithTemplateHorizontalSheetMock.generateMultiOrientedHorizontalSheets()));

        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Callable<Workbook>> demo : demos.entrySet()) {
            Workbook wb = demo.getValue().call();
            assertNotNull(wb, demo.getKey());
            try {
                lines.add("#### " + demo.getKey() + " styles=" + wb.getNumCellStyles());
                dump(wb, lines);
            }
            finally {
                ExcelSheetWriter.dispose(wb);
            }
        }
        Path out = Paths.get("target", "demo-workbook-snapshot.txt");
        Files.createDirectories(out.getParent());
        Files.write(out, lines, StandardCharsets.UTF_8);
    }

    private static void dump(Workbook wb, List<String> lines) {
        DataFormatter formatter = new DataFormatter();
        for (Sheet sheet : wb) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    CellStyle s = cell.getCellStyle();
                    Font f = wb.getFontAt(s.getFontIndex());
                    lines.add(sheet.getSheetName() + "!" + cell.getAddress() + " [" + formatter.formatCellValue(cell) + "]"
                            + " fmt=" + s.getDataFormatString() + " wrap=" + s.getWrapText() + " hidden=" + s.getHidden()
                            + " fill=" + s.getFillPattern() + "/" + s.getFillForegroundColor() + "/" + s.getFillBackgroundColor()
                            + " border=" + s.getBorderTop() + s.getBorderRight() + s.getBorderBottom() + s.getBorderLeft()
                            + "/" + s.getTopBorderColor()
                            + " font=" + f.getBold() + f.getItalic() + f.getStrikeout() + "/" + f.getColor() + "/" + f.getUnderline()
                            + "/" + f.getFontName() + "/" + f.getFontHeight()
                            + " colHidden=" + sheet.isColumnHidden(cell.getColumnIndex()) + " width=" + sheet.getColumnWidth(cell.getColumnIndex()));
                }
            }
        }
    }
}
