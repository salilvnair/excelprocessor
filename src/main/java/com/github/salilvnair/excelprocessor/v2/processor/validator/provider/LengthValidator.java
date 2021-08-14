package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;

public class LengthValidator extends BaseCellValidator {
    private final Field field;
    private String message;
    public LengthValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        if(cellValidation.length() > -1) {
            message = headerKey + " length should be "+cellValidation.length();
            return !((fieldValue+"").length() == cellValidation.length());
        }
        else if(cellValidation.minLength() > -1) {
            message = "Minimum length of "+headerKey+" should be less greater than or equal to "+cellValidation.minLength();
            return !((fieldValue+"").length() >= cellValidation.minLength());
        }
        else if(cellValidation.maxLength() > -1) {
            message = "Maximum length of "+headerKey+" should be less than or equal to "+cellValidation.minLength();
            return !((fieldValue+"").length() <= cellValidation.maxLength());
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
