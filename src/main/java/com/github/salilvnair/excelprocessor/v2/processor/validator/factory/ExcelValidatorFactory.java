package com.github.salilvnair.excelprocessor.v2.processor.validator.factory;

import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.IExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        else if(ExcelSheetValidatorType.COLUMN.equals(excelSheetValidatorType)) {
            validators = generateColumnValidators((Field) classInstance);
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

    private static List<IExcelValidator> generateColumnValidators(Field column) {
        List<IExcelValidator> validators = new ArrayList<>();
        CellValidation cellValidation = column.getAnnotation(CellValidation.class);
        if(cellValidation.required()) {
            validators.add(new RequiredValidator(column));
        }
        else if(cellValidation.conditional()) {
            validators.add(new ConditionalValidator(column));
        }
        if(cellValidation.date()) {
            validators.add(new DateValidator(column));
        }
        if(cellValidation.unique()) {
            validators.add(new UniqueValidator(column));
        }
        if(cellValidation.alphaNumeric()) {
            validators.add(new AlphaNumericValidator(column));
        }
        if(cellValidation.numeric()) {
            validators.add(new NumericValidator(column));
        }
        if(cellValidation.email()) {
            validators.add(new EmailValidator(column));
        }
        if(cellValidation.length() > -1 || cellValidation.minLength() > -1 || cellValidation.maxLength() > -1) {
            validators.add(new LengthValidator(column));
        }
        if(!StringUtils.isEmpty(cellValidation.customTask()) || cellValidation.customTasks().length > 0) {
            validators.add(new CustomMethodValidator(column));
        }
        if(!StringUtils.isEmpty(cellValidation.pattern()) && (cellValidation.matchPattern() || cellValidation.findPattern())) {
            validators.add(new PatternValidator(column));
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
