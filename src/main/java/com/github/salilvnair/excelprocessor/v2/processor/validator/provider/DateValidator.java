package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
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
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        if(fieldValue instanceof String && cellValidation.date()) {
            return DateParsingUtil.isDate(fieldValue+"", cellValidation.datePattern().value());
        }
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.DATE;
    }

}
