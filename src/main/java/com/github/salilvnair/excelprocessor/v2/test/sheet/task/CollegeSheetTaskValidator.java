package com.github.salilvnair.excelprocessor.v2.test.sheet.task;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.test.sheet.CollegeSheet;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.lang.reflect.Field;

/**
 * @author Salil V Nair
 */
public class CollegeSheetTaskValidator extends AbstractExcelTaskValidator {

    public String shouldBeGreaterThanZero(CellValidatorContext context) {
        CollegeSheet sheet = context.sheet(CollegeSheet.class);
        long noOfS = sheet.getNoOfStudents();
        if(noOfS<=0) {
            return "Min Students should be greater than 0";
        }
        return null;
    }

    public void highlightYellowIfValueIsEmpty(ExcelSheetWriterContext context) throws DecoderException {

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
