package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.fixed;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;

public class ConditionalValidator extends BaseCellValidator {
    private final Field field;
    private String conditionalMessage;
    public ConditionalValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        Object object = ExcelValidatorTaskExecutor.execute(cellValidation.condition(), sheet, validatorContext);
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
