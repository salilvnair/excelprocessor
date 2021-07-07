package com.github.salilvnair.excelprocessor.v1.test.task;

import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v1.reflect.service.AbstractCustomValidatorTask;
import com.github.salilvnair.excelprocessor.v1.test.sheet.SchoolSheet;

public class SchoolSheetTask extends AbstractCustomValidatorTask {

    public String noOfStudentCheck(ValidatorContext context) {
        SchoolSheet sheet = (SchoolSheet) context.getBaseExcelValidationSheet();
        if(sheet.getNoOfStudents() < 4000) {
            return "This is a chota school";
        }
        return null;
    }

}
