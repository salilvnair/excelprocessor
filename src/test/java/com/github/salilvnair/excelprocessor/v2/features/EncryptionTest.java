package com.github.salilvnair.excelprocessor.v2.features;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.features.sheet.OrderSheet;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.WorkbookCrypto;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** B4: password-protected workbooks on write and read; C3: messages print without Gson. */
class EncryptionTest {

    private static final String PASSWORD = "s3cret-Pa55";

    @TempDir
    Path dir;

    private static List<OrderSheet> rows() {
        return Arrays.asList(OrderSheet.of("A-1", "Acme", "Open", 10.0), OrderSheet.of("A-2", "Bolt", "Closed", 20.0));
    }

    @Test
    void listWriterEncryptsAndBothReadersDecrypt() throws Exception {
        File file = dir.resolve("secret.xlsx").toFile();
        try (OutputStream out = new FileOutputStream(file)) {
            ExcelSheetWriterFactory.generate().write(rows(), ExcelSheetContext.builder().password(PASSWORD).build(), out);
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            assertEquals(FileMagic.OLE2, FileMagic.valueOf(in), "encrypted XLSX is an OLE2 container");
        }
        assertTrue(WorkbookCrypto.isEncrypted(file));

        List<OrderSheet> listRead = ExcelSheetReaderFactory.generate()
                .read(OrderSheet.class, ExcelSheetContext.builder().excelFile(file).password(PASSWORD).build());
        assertEquals("Bolt", listRead.get(1).getCustomer());

        try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file, ExcelSheetContext.builder().password(PASSWORD).build())) {
            assertEquals(20.0, reader.readAll(OrderSheet.class).get(1).getAmount());
        }
        long leftovers = Files.list(Path.of(System.getProperty("java.io.tmpdir")))
                .filter(p -> p.getFileName().toString().startsWith("excelprocessor-decrypted")).count();
        assertEquals(0, leftovers, "the decrypted temp copy is deleted on close");
    }

    @Test
    void streamingWriterEncrypts() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(ExcelSheetContext.builder().password(PASSWORD).build())) {
            writer.beginSheet(OrderSheet.class);
            rows().forEach(writer::writeRow);
            writer.finish(out);
        }
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()), PASSWORD)) {
            assertEquals("A-2", wb.getSheet("Orders").getRow(2).getCell(0).getStringCellValue());
        }
    }

    @Test
    void wrongPasswordIsReportedClearly() throws Exception {
        File file = dir.resolve("secret.xlsx").toFile();
        try (OutputStream out = new FileOutputStream(file)) {
            ExcelSheetWriterFactory.generate().write(rows(), ExcelSheetContext.builder().password(PASSWORD).build(), out);
        }
        ExcelSheetReaderException e = assertThrows(ExcelSheetReaderException.class,
                () -> ExcelSheetReaderFactory.openStreaming(file, ExcelSheetContext.builder().password("wrong").build()));
        assertTrue(e.getMessage().contains("password"), e.getMessage());
    }

    @Test
    void xlsWorkbooksUseBiff8Encryption() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            wb.createSheet("Orders").createRow(0).createCell(0).setCellValue("Order Id");
            WorkbookCrypto.write(wb, out, PASSWORD);
        }
        try (Workbook wb = WorkbookCrypto.open(new ByteArrayInputStream(out.toByteArray()), PASSWORD)) {
            assertEquals("Order Id", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
        assertThrows(Exception.class, () -> WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray())).close());
    }

    @Test
    void validationMessagePrintsAsJsonWithoutGson() {
        CellValidationMessage message = new CellValidationMessage();
        message.setHeader("Customer");
        message.setMessage("Customer is \"required\"\n");
        message.setRow(3);
        message.setColumn("B");
        message.setValidatorType(ValidatorType.REQUIRED);
        assertEquals("\n{\"header\":\"Customer\",\"message\":\"Customer is \\\"required\\\"\\n\",\"row\":3,\"column\":\"B\",\"validatorType\":\"REQUIRED\"}",
                message.toString());
        assertEquals("B3", message.cellReference());
    }
}
