package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.helper.ObjectUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import java.lang.reflect.Field;

public class RequiredValidator extends BaseCellValidator {
    private final Field field;

    public RequiredValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        return ObjectUtils.isNull(fieldValue) || ObjectUtils.isEmptyString(fieldValue);
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.REQUIRED;
    }

    @Override
    protected String defaultMessage() {
        return "field cannot be null or empty.";
    }
}
