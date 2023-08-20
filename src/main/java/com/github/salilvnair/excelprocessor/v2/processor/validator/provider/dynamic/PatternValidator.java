package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternValidator extends BaseDynamicCellValidator {


    public PatternValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        CellValidationInfo cellValidation = cellValidation();
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        String fieldStringValue = (String) fieldValue;
        String pattern = cellValidation.getPattern();
        if(cellValidation.isFindPattern()) {
            return !find(pattern, fieldStringValue);
        }
        if(cellValidation.isMatchPattern()) {
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
        String header = header();
        return header+" is not a valid pattern.";
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
