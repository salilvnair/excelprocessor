package com.github.salilvnair.excelprocessor.spring;

import com.github.salilvnair.excelprocessor.spring.web.ExcelExportHandlerRegistrar;
import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.outline.WorkbookOutline;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class WebExportAndImportTest {

    static final AtomicBoolean STREAM_CLOSED = new AtomicBoolean();

    @RestController
    static class OrderController {
        @GetMapping("/orders.xlsx")
        ExcelExport<OrderRow> xlsx() {
            return ExcelExport.of(OrderRow.class, () -> IntStream.rangeClosed(1, 500)
                    .mapToObj(i -> new OrderRow("A-" + i, "Customer " + i, i * 1.5))
                    .onClose(() -> STREAM_CLOSED.set(true)));
        }

        @GetMapping("/orders.csv")
        ExcelExport<OrderRow> csv() {
            return ExcelExport.of(OrderRow.class, Arrays.asList(new OrderRow("A-1", "Zoë, Ltd", 2.0)))
                    .csv(CsvFormat.csv())
                    .fileName("Bestellungen März.csv");
        }
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig {
        @Bean
        OrderController orderController() {
            return new OrderController();
        }

        @Bean
        ExcelProcessorTemplate excelProcessorTemplate() {
            return new ExcelProcessorTemplate(null, new ExcelProcessorProperties());
        }

        @Bean
        static ExcelExportHandlerRegistrar excelExportHandlerRegistrar(ObjectProvider<ExcelProcessorTemplate> template) {
            return new ExcelExportHandlerRegistrar(template);
        }
    }

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebConfig.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void restControllerStreamsAnXlsxDownload() throws Exception {
        STREAM_CLOSED.set(false);
        MvcResult result = mvc.perform(get("/orders.xlsx")).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(ExcelExport.XLSX_CONTENT_TYPE, result.getResponse().getContentType());
        assertTrue(result.getResponse().getHeader("Content-Disposition").contains("filename=\"Orders.xlsx\""));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            Sheet sheet = wb.getSheet("Orders");
            assertEquals(500, sheet.getLastRowNum());
            assertEquals("A-500", sheet.getRow(500).getCell(0).getStringCellValue());
        }
        assertTrue(STREAM_CLOSED.get(), "the row stream is closed after writing");
    }

    @Test
    void csvDownloadUsesAnEncodedFileName() throws Exception {
        MvcResult result = mvc.perform(get("/orders.csv")).andReturn();
        assertEquals("text/csv;charset=UTF-8", result.getResponse().getContentType());
        String disposition = result.getResponse().getHeader("Content-Disposition");
        assertTrue(disposition.contains("filename*=UTF-8''Bestellungen%20M%C3%A4rz.csv"), disposition);
        assertEquals("Order Id,Customer,Amount\r\nA-1,\"Zoë, Ltd\",2\r\n", result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void importValidatesRowsAndOutlineDescribesTheUpload() throws Exception {
        ByteArrayOutputStream upload = new ByteArrayOutputStream();
        try (var writer = ExcelSheetWriterFactory.openStreaming()) {
            writer.beginSheet(OrderRow.class);
            writer.writeRow(new OrderRow("A-1", "Acme", 1.0));
            writer.writeRow(new OrderRow("A-2", null, 2.0));
            writer.writeRow(new OrderRow("A-1", "Bolt", 3.0));
            writer.finish(upload);
        }
        ExcelProcessorTemplate template = context.getBean(ExcelProcessorTemplate.class);
        List<String> saved = new ArrayList<>();
        ValidationReport report = template.importRows(new ByteArrayInputStream(upload.toByteArray()), "orders.xlsx", OrderRow.class,
                (row, messages) -> {
                    if (messages.isEmpty()) {
                        saved.add(row.getOrderId());
                    }
                });
        assertEquals(3, report.rowsRead());
        assertEquals(2, report.rowsWithErrors());
        assertEquals(List.of("A-1"), saved);

        WorkbookOutline outline = template.outline(new ByteArrayInputStream(upload.toByteArray()), "orders.xlsx");
        assertEquals(List.of("Order Id", "Customer", "Amount"), outline.sheet("Orders").headers());
        assertEquals(3, outline.sheet("Orders").dataRows());

        ByteArrayOutputStream csv = new ByteArrayOutputStream();
        ExcelExport.of(OrderRow.class, () -> Stream.of(new OrderRow("C-1", "Csv", 1.0))).csv(null).writeTo(csv);
        ValidationReport csvReport = template.importRows(new ByteArrayInputStream(csv.toByteArray()), "upload.CSV", OrderRow.class, (r, m) -> { });
        assertEquals(1, csvReport.rowsRead());
        assertFalse(csvReport.hasErrors());
    }
}
