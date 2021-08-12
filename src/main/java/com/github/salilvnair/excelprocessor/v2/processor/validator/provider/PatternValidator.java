package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.AbstractExcelValidator;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternValidator extends AbstractExcelValidator {
    @Override
    public List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext) {
        return null;
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
}
