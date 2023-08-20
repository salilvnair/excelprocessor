package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

public class DateValidator extends BaseDynamicCellValidator {

    public DateValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidationInfo cellValidation = cellValidation();
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        if(fieldValue instanceof String && cellValidation.isDate()) {
            return DateParsingUtil.isDate(fieldValue+"", cellValidation.getDatePattern().value());
        }
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.DATE;
    }

}
