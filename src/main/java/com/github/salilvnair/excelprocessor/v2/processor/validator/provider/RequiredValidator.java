package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.helper.ObjectUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import java.lang.reflect.Field;

public class RequiredValidator extends BaseCellValidator {

    public RequiredValidator(Field field) {
        super(field);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return ObjectUtils.isNull(fieldValue) || ObjectUtils.isEmptyString(fieldValue);
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.REQUIRED;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        return headerKey+" cannot be null or empty.";
    }
}
