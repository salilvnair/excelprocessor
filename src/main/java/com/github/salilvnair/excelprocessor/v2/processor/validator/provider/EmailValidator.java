package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class EmailValidator extends BaseCellValidator {

    public EmailValidator(Field field) {
        super(field);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
        String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
        String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";
        Pattern pattern = Pattern.compile("^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$", 2);
        return !PatternValidator.match(pattern, fieldValue+"");
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.EMAIL;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        return headerKey+" has invalid email format.";
    }
}
