package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.fixed;

import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;

public class CustomMethodValidator extends BaseCellValidator {

    private final Field field;

    public CustomMethodValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        if(!StringUtils.isEmpty(cellValidation.customTask())) {
            ExcelValidatorTaskExecutor.execute(cellValidation.customTask(), sheet.excelTaskValidator(), validatorContext);
        }
        else {
            for (String customTask : cellValidation.customTasks()) {
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
