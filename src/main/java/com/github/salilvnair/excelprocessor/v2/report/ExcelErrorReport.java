package com.github.salilvnair.excelprocessor.v2.report;

import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.processor.helper.WorkbookCrypto;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Writes validation messages back into a copy of the uploaded workbook: failing cells are filled
 * with a highlight colour and get a comment with their messages, and an "Errors" sheet lists every
 * message with a link to its cell. The uploaded file itself is never modified.
 *
 * <pre>{@code
 * List<CellValidationMessage> messages = reader.validate(rows, context);
 * ExcelErrorReport.of(uploadedFile, messages)
 *         .highlight(IndexedColors.ROSE)
 *         .writeTo(outputStream);
 * }</pre>
 *
 * Highlighted cells keep their own format: one highlighted style is created per distinct original
 * style, so the workbook's style count grows by at most the number of styles in use.
 */
public final class ExcelErrorReport {
    private static final String[] ERROR_COLUMNS = {"Sheet", "Cell", "Row", "Column", "Header", "Message", "Type", "Rule"};

    private final Workbook workbook;
    private final boolean ownsWorkbook;
    private final List<CellValidationMessage> messages;
    private IndexedColors highlight = IndexedColors.ROSE;
    private boolean comments = true;
    private String errorsSheetName = "Errors";
    private String password;
    private boolean annotated;

    private ExcelErrorReport(Workbook workbook, boolean ownsWorkbook, Collection<CellValidationMessage> messages) {
        this.workbook = workbook;
        this.ownsWorkbook = ownsWorkbook;
        this.messages = messages == null ? Collections.emptyList() : new ArrayList<>(messages);
    }

    /** Annotates a copy of {@code uploadedFile} (XLSX or XLS). */
    public static ExcelErrorReport of(File uploadedFile, Collection<CellValidationMessage> messages) {
        return of(uploadedFile, null, messages);
    }

    /** Annotates a copy of an encrypted {@code uploadedFile}. The report is written unencrypted unless {@link #password(String)} is set. */
    public static ExcelErrorReport of(File uploadedFile, String uploadPassword, Collection<CellValidationMessage> messages) {
        try {
            return new ExcelErrorReport(WorkbookCrypto.open(new FileInputStream(uploadedFile), uploadPassword), true, messages);
        }
        catch (FileNotFoundException e) {
            throw new ExcelSheetWriterException(e);
        }
    }

    public static ExcelErrorReport of(File uploadedFile, ValidationReport report) {
        return of(uploadedFile, report.messages());
    }

    /** Annotates a workbook read from {@code uploadedStream}; the stream is closed. */
    public static ExcelErrorReport of(InputStream uploadedStream, Collection<CellValidationMessage> messages) {
        return new ExcelErrorReport(WorkbookCrypto.open(uploadedStream, null), true, messages);
    }

    /** Annotates {@code workbook} in place; the caller keeps ownership and closes it. */
    public static ExcelErrorReport of(Workbook workbook, Collection<CellValidationMessage> messages) {
        return new ExcelErrorReport(workbook, false, messages);
    }

    /** Fill colour of failing cells (default ROSE); null disables highlighting. */
    public ExcelErrorReport highlight(IndexedColors color) {
        this.highlight = color;
        return this;
    }

    /** Whether failing cells get a comment with their messages (default true). */
    public ExcelErrorReport comments(boolean comments) {
        this.comments = comments;
        return this;
    }

    /** Name of the summary sheet (default "Errors"); null leaves it out. */
    public ExcelErrorReport errorsSheet(String name) {
        this.errorsSheetName = name;
        return this;
    }

    /** Encrypts the written report with this password. */
    public ExcelErrorReport password(String password) {
        this.password = password;
        return this;
    }

    /** Annotates the workbook and returns it (for further changes). Idempotent per report. */
    public Workbook annotate() {
        if (annotated) {
            return workbook;
        }
        annotated = true;
        Map<String, List<CellValidationMessage>> byCell = new LinkedHashMap<>();
        for (CellValidationMessage message : messages) {
            Sheet sheet = sheetOf(message);
            if (sheet == null || message.getRow() < 1 || message.getColumn() == null) {
                continue;
            }
            byCell.computeIfAbsent(sheet.getSheetName() + "!" + message.getColumn() + message.getRow(), k -> new ArrayList<>()).add(message);
        }
        Map<Short, CellStyle> highlighted = new HashMap<>();
        CreationHelper helper = workbook.getCreationHelper();
        Map<Sheet, Drawing<?>> drawings = new HashMap<>();
        for (List<CellValidationMessage> cellMessages : byCell.values()) {
            CellValidationMessage first = cellMessages.get(0);
            Sheet sheet = sheetOf(first);
            int rowIndex = first.getRow() - 1;
            int columnIndex = ExcelSheetReader.toIndentNumber(first.getColumn()) - 1;
            Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(columnIndex) == null ? row.createCell(columnIndex) : row.getCell(columnIndex);
            if (highlight != null) {
                CellStyle original = cell.getCellStyle();
                cell.setCellStyle(highlighted.computeIfAbsent(original.getIndex(), idx -> {
                    CellStyle style = workbook.createCellStyle();
                    style.cloneStyleFrom(original);
                    style.setFillForegroundColor(highlight.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    return style;
                }));
            }
            if (comments) {
                StringBuilder text = new StringBuilder();
                for (CellValidationMessage message : cellMessages) {
                    if (text.length() > 0) {
                        text.append('\n');
                    }
                    text.append(message.getMessage() == null ? String.valueOf(message.getValidatorType()) : message.getMessage());
                }
                if (cell.getCellComment() != null) {
                    text.insert(0, cell.getCellComment().getString().getString() + "\n");
                    cell.removeCellComment();
                }
                Drawing<?> drawing = drawings.computeIfAbsent(sheet, Sheet::createDrawingPatriarch);
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(columnIndex + 1);
                anchor.setCol2(columnIndex + 4);
                anchor.setRow1(rowIndex);
                anchor.setRow2(rowIndex + 3 + cellMessages.size());
                Comment comment = drawing.createCellComment(anchor);
                comment.setString(helper.createRichTextString(text.toString()));
                comment.setAuthor("excelprocessor");
                cell.setCellComment(comment);
            }
        }
        if (errorsSheetName != null) {
            writeErrorsSheet();
        }
        return workbook;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        annotate();
        WorkbookCrypto.write(workbook, outputStream, password);
        if (ownsWorkbook) {
            workbook.close();
        }
    }

    public void writeTo(Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            writeTo(out);
        }
    }

    private Sheet sheetOf(CellValidationMessage message) {
        if (message.getSheet() != null) {
            return workbook.getSheet(message.getSheet());
        }
        return workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
    }

    private void writeErrorsSheet() {
        String name = WorkbookUtil.createSafeSheetName(errorsSheetName);
        String unique = name;
        for (int i = 2; workbook.getSheet(unique) != null; i++) {
            unique = WorkbookUtil.createSafeSheetName(name + " (" + i + ")");
        }
        Sheet errors = workbook.createSheet(unique);
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(bold);
        Font linkFont = workbook.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        CellStyle linkStyle = workbook.createCellStyle();
        linkStyle.setFont(linkFont);

        Row header = errors.createRow(0);
        for (int c = 0; c < ERROR_COLUMNS.length; c++) {
            org.apache.poi.ss.usermodel.Cell cell = header.createCell(c);
            cell.setCellValue(ERROR_COLUMNS[c]);
            cell.setCellStyle(headerStyle);
        }
        CreationHelper helper = workbook.getCreationHelper();
        int r = 1;
        int maxRows = workbook.getSpreadsheetVersion().getLastRowIndex();
        for (CellValidationMessage message : messages) {
            if (r > maxRows) {
                break;
            }
            Sheet target = sheetOf(message);
            String sheetName = target == null ? message.getSheet() : target.getSheetName();
            Row row = errors.createRow(r++);
            row.createCell(0).setCellValue(sheetName == null ? "" : sheetName);
            String ref = message.cellReference();
            org.apache.poi.ss.usermodel.Cell refCell = row.createCell(1);
            refCell.setCellValue(ref == null ? "" : ref);
            if (ref != null && target != null) {
                Hyperlink link = helper.createHyperlink(HyperlinkType.DOCUMENT);
                link.setAddress("'" + target.getSheetName().replace("'", "''") + "'!" + ref);
                refCell.setHyperlink(link);
                refCell.setCellStyle(linkStyle);
            }
            if (message.getRow() > 0) {
                row.createCell(2).setCellValue(message.getRow());
            }
            row.createCell(3).setCellValue(message.getColumn() == null ? "" : message.getColumn());
            row.createCell(4).setCellValue(message.getOriginalHeader() != null ? message.getOriginalHeader() : message.getHeader() == null ? "" : message.getHeader());
            row.createCell(5).setCellValue(message.getMessage() == null ? "" : message.getMessage());
            row.createCell(6).setCellValue(message.getMessageType() == null ? "" : message.getMessageType());
            row.createCell(7).setCellValue(message.getValidatorType() == null ? "" : message.getValidatorType().name());
        }
        errors.createFreezePane(0, 1);
        errors.setAutoFilter(new CellRangeAddress(0, Math.max(0, r - 1), 0, ERROR_COLUMNS.length - 1));
        for (int c = 0; c < ERROR_COLUMNS.length; c++) {
            errors.autoSizeColumn(c);
        }
        workbook.setActiveSheet(workbook.getSheetIndex(errors));
        workbook.setSelectedTab(workbook.getSheetIndex(errors));
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (i != workbook.getSheetIndex(errors)) {
                workbook.getSheetAt(i).setSelected(false);
            }
        }
    }
}
