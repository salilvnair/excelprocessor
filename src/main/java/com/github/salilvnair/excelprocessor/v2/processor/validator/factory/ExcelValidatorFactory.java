package com.github.salilvnair.excelprocessor.v2.processor.validator.factory;

import com.github.salilvnair.excelprocessor.v2.annotation.AllowedValues;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.IExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
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

    public static List<IExcelValidator> generate(Object classInstance, ExcelSheetValidatorType excelSheetValidatorType) {
        List<IExcelValidator> validators = new ArrayList<>();
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

    private static List<IExcelValidator> generateSheetValidators(Object classInstance) {
        List<IExcelValidator> validators = new ArrayList<>();
        List<?> nodeList = (List<?>) classInstance;
        validators.add(new SheetValidator(nodeList));
        return validators;
    }

    private static List<IExcelValidator> generateCellValidators(Field field) {
        List<IExcelValidator> validators = new ArrayList<>();

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

    private static Collection<? extends IExcelValidator> generateAllowedValueValidators(Field field, CellValidation cellValidation) {
        List<IExcelValidator> validators = new ArrayList<>();
        if(cellValidation.required()) {
            validators.add(new AllowedValueValidator(field));
        }
        return validators;
    }

    private static List<IExcelValidator> generateCellValidators(Field field, CellValidation cellValidation) {
        List<IExcelValidator> validators = new ArrayList<>();
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

    private static List<IExcelValidator> generateRowValidators(Object classInstance) {
        List<IExcelValidator> validators = new ArrayList<>();
        validators.add(new RowValidator(classInstance));
        return validators;
    }

    private static List<IExcelValidator> generateSectionValidators(Field sectionField) {
        List<IExcelValidator> validators = new ArrayList<>();
        validators.add(new SectionValidator(sectionField));
        return validators;
    }

}
