package com.github.salilvnair.excelprocessor.v2.test.sheet.task;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.test.sheet.CollegeSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.SchoolSheet;

/**
 * @author Salil V Nair
 */
public class CollegeSheetTaskValidator extends AbstractExcelTaskValidator {
    public String shouldBeGreaterThanZero(ValidatorContext context) {
        CollegeSheet sheet = context.sheet(CollegeSheet.class);
        if(sheet.getNoOfStudents()<=0) {
            return "Min Students should be greater than 0";
        }
        return null;
    }
    public void defaultUniversity(ValidatorContext context) {
        CollegeSheet sheet = context.sheet(CollegeSheet.class);
        sheet.setUniversity("Anna University");
    }
}
