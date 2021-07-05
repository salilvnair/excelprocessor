package com.github.salilvnair.excelprocessor.test.task;

import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.reflect.service.AbstractCustomValidatorTask;
import com.github.salilvnair.excelprocessor.test.sheet.SchoolSheet;

public class SchoolSheetTask extends AbstractCustomValidatorTask {

    public String noOfStudentCheck(ValidatorContext context) {
        SchoolSheet sheet = (SchoolSheet) context.getBaseExcelValidationSheet();
        if(sheet.getNoOfStudents() < 4000) {
            return "This is a chota school";
        }
        return null;
    }

}
