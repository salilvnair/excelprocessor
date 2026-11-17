package com.github.salilvnair.excelprocessor.v2.writer.sheet;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * Typical user task that mutates "the cell's style" in place. Before the style cache this
 * created/mutated one style per cell; now it must only affect the empty cells.
 */
public class HighlightTask extends AbstractExcelTask {
    public void highlightEmpty(ExcelSheetWriterContext context) {
        if (context.cellValue() == null) {
            context.rowCell().getCellStyle().setFillPattern(FillPatternType.SOLID_FOREGROUND);
            context.rowCell().getCellStyle().setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        }
    }
}
