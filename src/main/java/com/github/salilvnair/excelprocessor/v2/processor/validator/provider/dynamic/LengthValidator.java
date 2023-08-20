package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

public class LengthValidator extends BaseDynamicCellValidator {

    private String message;

    public LengthValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }


    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidationInfo cellValidation = cellValidation();
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        String header = header();
        if(cellValidation.getLength() > -1) {
            message = header + " length should be "+cellValidation.getLength();
            return !((fieldValue+"").length() == cellValidation.getLength());
        }
        else if(cellValidation.getMinLength() > -1) {
            message = "Minimum length of "+header+" should be less greater than or equal to "+cellValidation.getMinLength();
            return !((fieldValue+"").length() >= cellValidation.getMinLength());
        }
        else if(cellValidation.getMaxLength() > -1) {
            message = "Maximum length of "+header+" should be less than or equal to "+cellValidation.getMaxLength();
            return !((fieldValue+"").length() <= cellValidation.getMaxLength());
        }
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.LENGTH;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return message;
    }
}
