package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

public class ConditionalValidator extends BaseDynamicCellValidator {

    private String conditionalMessage;

    public ConditionalValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }


    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidationInfo cellValidation = cellValidation();
        validatorContext.setHeader(header());
        validatorContext.setHeaderKey(headerKey());
        Object object = ExcelValidatorTaskExecutor.execute(cellValidation.getCondition(), sheet.excelTaskValidator(), validatorContext);
        if(!ObjectUtil.isNull(object) && !ObjectUtil.isBoolean(object)) {
            this.conditionalMessage = object+"";
        }
        return ObjectUtil.nonNullOrBooleanTrue(object);
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.CONDITIONAL;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return conditionalMessage;
    }
}
