package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternValidator extends BaseCellValidator {
    private final Field field;

    public PatternValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        String fieldStringValue = (String) fieldValue;
        String pattern = cellValidation.pattern();
        if(cellValidation.findPattern()) {
            return !find(pattern, fieldStringValue);
        }
        if(cellValidation.matchPattern()) {
            return !match(pattern, fieldStringValue);
        }
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.REQUIRED;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        return headerKey+" is not a valid pattern.";
    }

    public static boolean match(String patternString, String inputString) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher m = pattern.matcher(inputString);
        return m.matches();
    }

    public static boolean find(String patternString, String inputString) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher m = pattern.matcher(inputString);
        return m.find();
    }

    public static boolean match(Pattern pattern, String inputString) {
        Matcher m = pattern.matcher(inputString);
        return m.matches();
    }

    public static boolean find(Pattern pattern, String inputString) {
        Matcher m = pattern.matcher(inputString);
        return m.find();
    }
}
