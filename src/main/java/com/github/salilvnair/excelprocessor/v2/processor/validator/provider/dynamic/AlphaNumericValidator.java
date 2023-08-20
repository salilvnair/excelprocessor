package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;


public class AlphaNumericValidator extends BaseDynamicCellValidator {


    public AlphaNumericValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        CellValidationInfo cellValidation = cellValidation();
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        return !PatternValidator.match("[A-Za-z0-9]+", fieldValue+"");
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.ALPHANUMERIC;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String header = header();
        return header+" is not a valid alphanumeric number.";
    }
}
