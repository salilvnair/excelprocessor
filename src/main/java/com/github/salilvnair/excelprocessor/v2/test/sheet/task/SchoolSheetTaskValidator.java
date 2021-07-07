package com.github.salilvnair.excelprocessor.v2.test.sheet.task;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.test.sheet.SchoolSheet;

/**
 * @author Salil V Nair
 */
public class SchoolSheetTaskValidator extends AbstractExcelTaskValidator {
    public String shouldBeGreaterThanZero(CellValidatorContext context) {
        SchoolSheet sheet = context.sheet(SchoolSheet.class);
        if(sheet.getNoOfStudents()<=0) {
            return "Min Students should be greater than 0";
        }
        return null;
    }
}
