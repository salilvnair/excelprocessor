package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

public class RequiredValidator extends BaseDynamicCellValidator {


    public RequiredValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return ObjectUtil.isNull(fieldValue) || ObjectUtil.isEmptyString(fieldValue);
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.REQUIRED;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String header = header();
        return header+" cannot be null or empty.";
    }
}
