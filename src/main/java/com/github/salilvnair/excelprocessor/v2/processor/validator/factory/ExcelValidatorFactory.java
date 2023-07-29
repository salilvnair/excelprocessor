package com.github.salilvnair.excelprocessor.v2.processor.validator.factory;

import com.github.salilvnair.excelprocessor.v2.annotation.AllowedValues;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Salil V Nair
 */
public class ExcelValidatorFactory {
    private ExcelValidatorFactory(){}

    public static List<ExcelValidator> generate(Object classInstance, ExcelSheetValidatorType excelSheetValidatorType) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(ExcelSheetValidatorType.ROW.equals(excelSheetValidatorType)) {
            validators = generateRowValidators(classInstance);
        }
        else if(ExcelSheetValidatorType.SHEET.equals(excelSheetValidatorType)) {
            validators = generateSheetValidators(classInstance);
        }
        else if(ExcelSheetValidatorType.CELL.equals(excelSheetValidatorType)) {
            validators = generateCellValidators((Field) classInstance);
        }
        else if(ExcelSheetValidatorType.SECTION.equals(excelSheetValidatorType)) {
            validators = generateSectionValidators((Field) classInstance);
        }
        return validators;
    }

    private static List<ExcelValidator> generateSheetValidators(Object classInstance) {
        List<ExcelValidator> validators = new ArrayList<>();
        List<?> nodeList = (List<?>) classInstance;
        validators.add(new SheetValidator(nodeList));
        return validators;
    }

    private static List<ExcelValidator> generateCellValidators(Field field) {
        List<ExcelValidator> validators = new ArrayList<>();

        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        if(cellValidation != null) {
            validators.addAll(generateCellValidators(field, cellValidation));
        }

        AllowedValues allowedValues = field.getAnnotation(AllowedValues.class);
        if(allowedValues != null) {
            validators.addAll(generateAllowedValueValidators(field, cellValidation));
        }

        return validators;
    }

    private static Collection<? extends ExcelValidator> generateAllowedValueValidators(Field field, CellValidation cellValidation) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(cellValidation.required()) {
            validators.add(new AllowedValueValidator(field));
        }
        return validators;
    }

    private static List<ExcelValidator> generateCellValidators(Field field, CellValidation cellValidation) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(cellValidation.required()) {
            validators.add(new RequiredValidator(field));
        }
        else if(cellValidation.conditional()) {
            validators.add(new ConditionalValidator(field));
        }
        if(cellValidation.date()) {
            validators.add(new DateValidator(field));
        }
        if(cellValidation.unique()) {
            validators.add(new UniqueValidator(field));
        }
        if(cellValidation.alphaNumeric()) {
            validators.add(new AlphaNumericValidator(field));
        }
        if(cellValidation.numeric()) {
            validators.add(new NumericValidator(field));
        }
        if(cellValidation.email()) {
            validators.add(new EmailValidator(field));
        }
        if(cellValidation.length() > -1 || cellValidation.minLength() > -1 || cellValidation.maxLength() > -1) {
            validators.add(new LengthValidator(field));
        }
        if(!StringUtils.isEmpty(cellValidation.customTask()) || cellValidation.customTasks().length > 0) {
            validators.add(new CustomMethodValidator(field));
        }
        if(!StringUtils.isEmpty(cellValidation.pattern()) && (cellValidation.matchPattern() || cellValidation.findPattern())) {
            validators.add(new PatternValidator(field));
        }
        return validators;
    }

    private static List<ExcelValidator> generateRowValidators(Object classInstance) {
        List<ExcelValidator> validators = new ArrayList<>();
        validators.add(new RowValidator(classInstance));
        return validators;
    }

    private static List<ExcelValidator> generateSectionValidators(Field sectionField) {
        List<ExcelValidator> validators = new ArrayList<>();
        validators.add(new SectionValidator(sectionField));
        return validators;
    }

}
