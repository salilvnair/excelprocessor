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
}
