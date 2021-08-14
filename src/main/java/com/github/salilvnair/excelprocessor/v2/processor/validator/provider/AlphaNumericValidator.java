package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;


public class AlphaNumericValidator extends BaseCellValidator {
    public AlphaNumericValidator(Field field) {
        super(field);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return !PatternValidator.match("[A-Za-z0-9]+", fieldValue+"");
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.ALPHANUMERIC;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        return headerKey+" is not a valid alphanumeric number.";
    }
}
