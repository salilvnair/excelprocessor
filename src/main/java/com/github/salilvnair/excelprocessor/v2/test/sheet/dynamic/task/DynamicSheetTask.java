package com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic.task;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import org.apache.commons.codec.DecoderException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

public class DynamicSheetTask extends AbstractExcelTask {

    public void highlightYellowIfValueIsEmpty(ExcelSheetWriterContext context, Object... taskMetadata) throws DecoderException {
        if(context.cellValue() == null) {
            Cell rowCell = context.rowCell();
            CellStyle cellStyle = rowCell.getSheet().getWorkbook().createCellStyle();

            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

//            String rgbS = "FFF000";
//            byte[] rgbB = Hex.decodeHex(rgbS); // get byte array from hex string
//            Color color = new XSSFColor(rgbB, null); //IndexedColorMap has no usage until now. So it can be set null.
//
//            cellStyle.setFillForegroundColor(color);
//            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            rowCell.setCellStyle(cellStyle);
        }
    }
}
