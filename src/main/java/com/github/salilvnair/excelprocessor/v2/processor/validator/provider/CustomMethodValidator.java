package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;

public class CustomMethodValidator extends BaseCellValidator {

    private final Field field;

    public CustomMethodValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        ExcelSheet excelSheet = validatorContext.excelSheet();
        ExcelHeaderValidator headerValidator = field.getAnnotation(ExcelHeaderValidator.class);
        ExcelValidatorTaskExecutor.execute(headerValidator.customTask(), excelSheet.excelTaskValidator(), validatorContext);
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.NA;
    }

}
