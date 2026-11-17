package com.github.salilvnair.excelprocessor.v2.processor.helper.style;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.NumberStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.StyleTemplateCellInfo;
import com.github.salilvnair.excelprocessor.v2.model.TextStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Helpers that translate the library's style annotations/infos into {@link CellStyleSpec}s and
 * apply resolved, shared styles to cells.
 *
 * @author Salil V Nair
 */
public final class CellStyleResolver {
    private CellStyleResolver() {}

    /**
     * Returns the style of the configured style-template cell, or {@code null} when the template
     * sheet/cell does not exist. Also copies the template column width to {@code rowCell}'s column.
     */
    public static CellStyle templateStyle(ExcelSheetWriterContext context, Sheet sheet, StyleTemplateCellInfo templateCell, Cell rowCell) {
        if (templateCell == null) {
            return null;
        }
        return templateStyle(context, sheet, templateCell.getRow(), templateCell.getColumn(), rowCell);
    }

    public static CellStyle templateStyle(ExcelSheetWriterContext context, Sheet sheet, int row, String column, Cell rowCell) {
        Workbook styleTemplate = context.styleTemplate();
        org.apache.poi.ss.usermodel.Sheet templateSheet = styleTemplate == null ? null : styleTemplate.getSheet(sheet.value());
        if (templateSheet == null) {
            return null;
        }
        int rowIndex = row - 1;
        int columnIndex = ExcelSheetReader.toIndentNumber(column) - 1;
        Row templateRow = templateSheet.getRow(rowIndex);
        Cell templateCell = templateRow == null ? null : templateRow.getCell(columnIndex);
        if (templateCell == null) {
            return null;
        }
        rowCell.getSheet().setColumnWidth(rowCell.getColumnIndex(), templateSheet.getColumnWidth(templateCell.getColumnIndex()));
        return templateCell.getCellStyle();
    }

    /**
     * Index of the cell's current style when it carries a non-default number format (for example
     * a style coming from a user-defined template) so that it is used as the base, otherwise -1.
     */
    public static int baseStyleIndex(Cell rowCell) {
        CellStyle current = rowCell.getCellStyle();
        if (current == null || current.getDataFormat() == 0) {
            return -1;
        }
        return current.getIndex();
    }

    public static FontSpec font(TextStyleInfo textStyleInfo) {
        if (textStyleInfo == null) {
            return null;
        }
        return FontSpec.builder()
                .bold(textStyleInfo.isBold())
                .italic(textStyleInfo.isItalic())
                .strikeout(textStyleInfo.isStrikeout())
                .color(textStyleInfo.getColor().getIndex())
                .fontName(StringUtils.isNotEmpty(textStyleInfo.getFontName()) ? textStyleInfo.getFontName() : null)
                .fontHeight(textStyleInfo.getFontHeight())
                .build();
    }

    public static String dataFormat(NumberStyleInfo numberStyleInfo) {
        if (numberStyleInfo == null) {
            return null;
        }
        if (numberStyleInfo.custom() && !numberStyleInfo.customFormat().isEmpty()) {
            return numberStyleInfo.customFormat();
        }
        if (numberStyleInfo.date()) {
            return numberStyleInfo.dateFormat().format();
        }
        if (numberStyleInfo.number()) {
            return numberStyleInfo.numberFormat().format();
        }
        return numberStyleInfo.categoryFormat().format();
    }

    /** Resolves {@code spec} to the shared workbook style and sets it on {@code rowCell}. */
    public static CellStyle apply(Cell rowCell, CellStyleSpec spec, ExcelSheetWriterContext context) {
        CellStyle style = context.styleRegistry().resolve(rowCell.getSheet().getWorkbook(), spec);
        rowCell.setCellStyle(style);
        return style;
    }

    /**
     * Runs user style tasks against a private scratch copy of the cell's style. Tasks written in the
     * common "{@code context.rowCell().getCellStyle().setXxx(..)}" form therefore only change this
     * cell; the result is deduplicated, so equal task outputs share one workbook style. Tasks that
     * set their own style on the cell (for example one cached via {@code context.addDataCellStyle})
     * are left untouched.
     */
    public static void runWithPrivateStyle(Cell rowCell, ExcelSheetWriterContext context, Runnable tasks) {
        Workbook workbook = rowCell.getSheet().getWorkbook();
        CellStyleRegistry registry = context.styleRegistry();
        CellStyle scratch = registry.taskScratch(workbook);
        scratch.cloneStyleFrom(rowCell.getCellStyle());
        rowCell.setCellStyle(scratch);
        try {
            tasks.run();
        }
        finally {
            CellStyle after = rowCell.getCellStyle();
            if (after != null && after.getIndex() == scratch.getIndex()) {
                rowCell.setCellStyle(registry.intern(workbook, scratch));
            }
        }
    }
}
