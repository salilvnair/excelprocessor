package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;

public class DateValidator extends BaseCellValidator {
    private final Field field;
    public DateValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.DATE;
    }

}
