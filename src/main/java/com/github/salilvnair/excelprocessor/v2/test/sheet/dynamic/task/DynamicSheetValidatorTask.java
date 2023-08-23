package com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic.task;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;

public class DynamicSheetValidatorTask extends AbstractExcelTaskValidator {

    public void someCustomTask(CellValidatorContext context) {
        System.out.println(context);
    }

}
