package com.github.salilvnair.excelprocessor.v2.test.sheet.task;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.test.sheet.CollegeSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.MultiOrientedCollegeSheet;

/**
 * @author Salil V Nair
 */
public class CollegeSheetTaskValidator extends AbstractExcelTaskValidator {
    public String shouldBeGreaterThanZero(CellValidatorContext context) {
        CollegeSheet sheet = context.sheet(CollegeSheet.class);
        long noOfS = 0;
        if(sheet == null) {
            MultiOrientedCollegeSheet mSheet = context.sheet(MultiOrientedCollegeSheet.class);
            noOfS = mSheet.getNoOfStudents();
        }
        else {
            noOfS = sheet.getNoOfStudents();
        }
        if(noOfS<=0) {
            return "Min Students should be greater than 0";
        }
        return null;
    }
    public void defaultUniversity(CellValidatorContext context) {
        CollegeSheet sheet = context.sheet(CollegeSheet.class);
        if(sheet == null) {
            MultiOrientedCollegeSheet mSheet = context.sheet(MultiOrientedCollegeSheet.class);
            mSheet.setUniversity("Appa University");
        }
        else {
            sheet.setUniversity("Anna University");
        }
    }
}
