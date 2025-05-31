package com.github.salilvnair.excelprocessor.v2.test.archived.sheet.task;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.test.archived.sheet.CollegeSheet;

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
