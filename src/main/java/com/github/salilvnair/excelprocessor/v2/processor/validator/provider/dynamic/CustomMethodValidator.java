package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

public class CustomMethodValidator extends BaseDynamicCellValidator {


    public CustomMethodValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidationInfo cellValidation = cellValidation();
        validatorContext.setHeader(header());
        validatorContext.setHeaderKey(headerKey());
        if(!StringUtils.isEmpty(cellValidation.getCustomTask())) {
            ExcelValidatorTaskExecutor.execute(cellValidation.getCustomTask(), sheet.excelTaskValidator(), validatorContext);
        }
        else {
            for (String customTask : cellValidation.getCustomTasks()) {
                ExcelValidatorTaskExecutor.execute(customTask, sheet.excelTaskValidator(), validatorContext);
            }
        }

        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.NA;
    }

}
