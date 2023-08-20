package com.github.salilvnair.excelprocessor.v2.processor.validator.factory.common;

import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.common.RowValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.common.SectionValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.common.SheetValidator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Salil V Nair
 */
public class ExcelCommonValidatorFactory {
    private ExcelCommonValidatorFactory(){}

    public static List<ExcelValidator> generateSheetValidators(Object classInstance) {
        List<ExcelValidator> validators = new ArrayList<>();
        List<?> nodeList = (List<?>) classInstance;
        validators.add(new SheetValidator(nodeList));
        return validators;
    }

    public static List<ExcelValidator> generateRowValidators(Object classInstance) {
        List<ExcelValidator> validators = new ArrayList<>();
        validators.add(new RowValidator(classInstance));
        return validators;
    }

    public static List<ExcelValidator> generateSectionValidators(Field sectionField) {
        List<ExcelValidator> validators = new ArrayList<>();
        validators.add(new SectionValidator(sectionField));
        return validators;
    }

}
