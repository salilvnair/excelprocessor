package com.github.salilvnair.excelprocessor.maven;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GenerateSheetsMojoTest {

    @TempDir
    Path dir;

    private File sampleWorkbook(Path excelDir) throws Exception {
        Files.createDirectories(excelDir);
        File file = excelDir.resolve("orders.xlsx").toFile();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Order lines");
            sheet.createRow(0).createCell(0).setCellValue("Exported 2026");   // title row above the headers
            Row h = sheet.createRow(2);
            String[] headers = {"Order #", "Amount", "Placed on", "Paid?", "Class", "Amount", "Note"};
            for (int i = 0; i < headers.length; i++) h.createCell(i + 1).setCellValue(headers[i]);
            CellStyle date = wb.createCellStyle();
            date.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
            for (int r = 3; r < 6; r++) {
                Row row = sheet.createRow(r);
                row.createCell(1).setCellValue("A-" + r);
                row.createCell(2).setCellValue(r * 10);
                row.createCell(3).setCellValue(LocalDate.of(2026, 1, r));
                row.getCell(3).setCellStyle(date);
                row.createCell(4).setCellValue(r % 2 == 0);
                row.createCell(5).setCellValue("gold");
                row.createCell(6).setCellValue(r + 0.25);
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        // headerRow (3) applies to every file
        Files.writeString(excelDir.resolve("people.csv"), "People export\n\nId,Name\n1,Ann\n");
        Files.writeString(excelDir.resolve("~$orders.xlsx"), "lock file");
        return file;
    }

    private GenerateSheetsMojo mojo(Path excelDir, Path out) {
        GenerateSheetsMojo mojo = new GenerateSheetsMojo();
        mojo.sourceDirectory = excelDir.toFile();
        mojo.outputDirectory = out.toFile();
        mojo.packageName = "com.acme.sheets";
        mojo.headerRow = 3;
        mojo.sampleRows = 1000;
        mojo.project = new MavenProject();
        return mojo;
    }

    @Test
    void generatedClassesCompileAndReadTheSample() throws Exception {
        Path excelDir = dir.resolve("src/main/excel");
        File workbook = sampleWorkbook(excelDir);
        Path out = dir.resolve("generated");
        GenerateSheetsMojo mojo = mojo(excelDir, out);
        mojo.execute();

        assertTrue(mojo.project.getCompileSourceRoots().contains(out.toFile().getAbsolutePath()));
        Path source = out.resolve("com/acme/sheets/OrderLinesSheet.java");
        String code = Files.readString(source);
        assertTrue(code.contains("@Sheet(value = \"Order lines\", headerRowAt = 3, headerColumnAt = \"B\", duplicateHeaders = true)"), code);
        assertTrue(code.contains("@Cell(\"Order #\")\n    private String orderNo;"), code);
        assertTrue(code.contains("@Cell(value = \"Amount\", column = \"C\")\n    private Long amount;"), code);
        assertTrue(code.contains("@Cell(value = \"Amount\", column = \"G\")\n    private Double amount2;"), code);
        assertTrue(code.contains("private Date placedOn;"), code);
        assertTrue(code.contains("private Boolean paid;"), code);
        assertTrue(code.contains("private String classValue;"), "keywords are renamed");
        assertTrue(code.contains("private String note;"), "an empty column is text");
        assertTrue(Files.exists(out.resolve("com/acme/sheets/PeopleSheet.java")), "CSV files are outlined too");

        // compile the generated code against the core library and read the sample with it
        Path classes = dir.resolve("classes");
        Files.createDirectories(classes);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String classpath = new File(BaseSheet.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int status = compiler.run(null, null, errors, "-d", classes.toString(), "-classpath", classpath,
                source.toString(), out.resolve("com/acme/sheets/PeopleSheet.java").toString());
        assertEquals(0, status, errors.toString());
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[] {classes.toUri().toURL()}, getClass().getClassLoader())) {
            @SuppressWarnings("unchecked")
            Class<? extends BaseSheet> type = (Class<? extends BaseSheet>) loader.loadClass("com.acme.sheets.OrderLinesSheet");
            List<? extends BaseSheet> rows = ExcelSheetReaderFactory.generate().read(type, ExcelSheetContext.builder().excelFile(workbook).build());
            assertEquals(3, rows.size());
            Method orderNo = type.getMethod("getOrderNo");
            Method amount = type.getMethod("getAmount");
            Method amount2 = type.getMethod("getAmount2");
            assertEquals("A-3", orderNo.invoke(rows.get(0)));
            assertEquals(30L, amount.invoke(rows.get(0)));
            assertEquals(3.25, amount2.invoke(rows.get(0)));
        }
    }

    @Test
    void unchangedFilesAreNotRewrittenAndSheetsCanBeFiltered() throws Exception {
        Path excelDir = dir.resolve("excel");
        sampleWorkbook(excelDir);
        Path out = dir.resolve("generated");
        GenerateSheetsMojo mojo = mojo(excelDir, out);
        mojo.sheets = Collections.singletonList("Order lines");
        mojo.execute();
        Path source = out.resolve("com/acme/sheets/OrderLinesSheet.java");
        long modified = Files.getLastModifiedTime(source).toMillis();
        assertFalse(Files.exists(out.resolve("com/acme/sheets/PeopleSheet.java")));
        Thread.sleep(20);
        mojo.execute();
        assertEquals(modified, Files.getLastModifiedTime(source).toMillis());
    }

    @Test
    void clashingClassNamesFailTheBuild() throws Exception {
        Path excelDir = dir.resolve("excel");
        Files.createDirectories(excelDir.resolve("a"));
        Files.createDirectories(excelDir.resolve("b"));
        Files.writeString(excelDir.resolve("a/people.csv"), "Id\n1\n");
        Files.writeString(excelDir.resolve("b/people.csv"), "Id\n1\n");
        GenerateSheetsMojo mojo = mojo(excelDir, dir.resolve("generated"));
        mojo.headerRow = -1;
        MojoExecutionException e = assertThrows(MojoExecutionException.class, mojo::execute);
        assertTrue(e.getMessage().contains("PeopleSheet"), e.getMessage());
    }

    @Test
    void missingSourceDirectoryIsNotAnError() throws Exception {
        GenerateSheetsMojo mojo = mojo(dir.resolve("none"), dir.resolve("generated"));
        mojo.execute();
        assertEquals(1, mojo.project.getCompileSourceRoots().size());
    }

    @Test
    void namesAreValidJavaIdentifiers() {
        Set<String> used = new HashSet<>();
        assertEquals("orderId", SheetSourceGenerator.fieldName("Order Id", used));
        assertEquals("orderId2", SheetSourceGenerator.fieldName("order-id", used));
        assertEquals("_2024", SheetSourceGenerator.fieldName("2024", used));
        assertEquals("S2024Sheet", SheetSourceGenerator.className("2024"));
        assertEquals("OrdersSheet", SheetSourceGenerator.className("orders"));
        assertEquals("SalesSheet", SheetSourceGenerator.className("Sales Sheet"));
    }
}
