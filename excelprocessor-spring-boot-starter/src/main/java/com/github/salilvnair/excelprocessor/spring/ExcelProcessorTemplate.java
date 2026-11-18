package com.github.salilvnair.excelprocessor.spring;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.outline.OutlineOptions;
import com.github.salilvnair.excelprocessor.v2.outline.WorkbookOutline;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Entry point for Spring applications. Every context it builds resolves task and validator beans
 * from the application context, so {@code excelTaskBeanName}, {@code excelTaskValidatorBeanName}
 * and bean-named custom tasks work without wiring.
 *
 * <pre>{@code
 * ValidationReport report = excel.importRows(upload.getInputStream(), upload.getOriginalFilename(),
 *         OrderSheet.class, (row, messages) -> { if (messages.isEmpty()) repository.save(row.toEntity()); });
 * }</pre>
 */
public class ExcelProcessorTemplate {
    private final Function<String, Object> beanResolver;
    private final ExcelProcessorProperties properties;

    public ExcelProcessorTemplate(Function<String, Object> beanResolver, ExcelProcessorProperties properties) {
        this.beanResolver = beanResolver;
        this.properties = properties == null ? new ExcelProcessorProperties() : properties;
    }

    /** A context builder with the bean resolver preset; finish it with {@link #build(ExcelSheetContext.ExcelSheetContextBuilder)}. */
    public ExcelSheetContext.ExcelSheetContextBuilder contextBuilder() {
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        if (beanResolver != null && properties.isResolveBeans()) {
            builder.beanResolver(beanResolver);
        }
        return builder;
    }

    /** Builds the context and applies the {@code excelprocessor.*} settings. */
    public ExcelSheetContext build(ExcelSheetContext.ExcelSheetContextBuilder builder) {
        ExcelSheetContext context = builder.build();
        if (context.beanFunction() == null && beanResolver != null && properties.isResolveBeans()) {
            context.setBeanResolver(beanResolver);
        }
        if (properties.isFailOnTaskExceptions()) {
            context.setSuppressTaskExceptions(false);
        }
        return context;
    }

    public ExcelSheetContext context() {
        return build(contextBuilder());
    }

    public ExcelSheetReader reader() {
        return ExcelSheetReaderFactory.generate();
    }

    public ExcelSheetWriter writer() {
        return ExcelSheetWriterFactory.generate();
    }

    /** Reads every row of {@code sheetClass} from an XLSX or XLS file into a list. */
    public <T extends BaseSheet> List<T> read(File file, Class<T> sheetClass) {
        return reader().read(sheetClass, build(contextBuilder().excelFile(file)));
    }

    /** Opens a streaming reader; XLSX, or CSV/TSV by file extension. */
    public StreamingExcelSheetReader openStreaming(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".csv")) {
            return ExcelSheetReaderFactory.openCsv(file, CsvFormat.csv(), context());
        }
        if (name.endsWith(".tsv")) {
            return ExcelSheetReaderFactory.openCsv(file, CsvFormat.tsv(), context());
        }
        return ExcelSheetReaderFactory.openStreaming(file, context());
    }

    public StreamingExcelSheetWriter openStreamingWriter() {
        return ExcelSheetWriterFactory.openStreaming(context());
    }

    /**
     * Imports an upload row by row: the stream is copied to a temporary file (so even large XLSX
     * files are read with flat memory), each row is validated and handed to {@code rowHandler}
     * with its messages. The temporary file is deleted afterwards.
     *
     * @param fileName the original file name; its extension selects XLSX, CSV or TSV
     */
    public <T extends BaseSheet> ValidationReport importRows(InputStream upload, String fileName, Class<T> sheetClass,
                                                           BiConsumer<? super T, List<CellValidationMessage>> rowHandler) {
        Path temp = copyToTemp(upload, fileName);
        try (StreamingExcelSheetReader reader = openStreaming(temp.toFile())) {
            return reader.validate(sheetClass, rowHandler);
        }
        finally {
            deleteQuietly(temp);
        }
    }

    /** Sheets, headers and column types of an upload, e.g. for a column-mapping step. */
    public WorkbookOutline outline(InputStream upload, String fileName) {
        Path temp = copyToTemp(upload, fileName);
        try {
            return ExcelSheetReaderFactory.outline(temp.toFile(), OutlineOptions.defaults());
        }
        finally {
            deleteQuietly(temp);
        }
    }

    private static Path copyToTemp(InputStream upload, String fileName) {
        String suffix = ".xlsx";
        if (fileName != null && fileName.lastIndexOf('.') >= 0) {
            String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (extension.matches("\\.[a-z0-9]{1,5}")) {
                suffix = extension;
            }
        }
        Path temp = null;
        try (InputStream in = upload) {
            temp = Files.createTempFile("excelprocessor-upload", suffix);
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            return temp;
        }
        catch (IOException e) {
            if (temp != null) {
                deleteQuietly(temp);
            }
            throw new UncheckedIOException(e);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        }
        catch (IOException ignored) {
            // the temp directory is cleaned eventually
        }
    }
}
