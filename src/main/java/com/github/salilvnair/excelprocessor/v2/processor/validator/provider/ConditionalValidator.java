package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.helper.ObjectUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
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
    protected boolean violated(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        ExcelSheet excelSheet = validatorContext.excelSheet();
        ExcelHeaderValidator headerValidator = field.getAnnotation(ExcelHeaderValidator.class);
        Object object = ExcelValidatorTaskExecutor.execute(headerValidator.condition(),excelSheet.excelTaskValidator(), validatorContext);
        if(!ObjectUtils.isNull(object) && !ObjectUtils.isBoolean(object)) {
            this.conditionalMessage = object+"";
        }
        return ObjectUtils.nonNullOrBooleanTrue(object);
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.CONDITIONAL;
    }

    @Override
    protected String defaultMessage() {
        return conditionalMessage;
    }
}
