package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.AbstractExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelValidatorUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Salil V Nair
 */
public class SectionValidator extends AbstractExcelValidator {
    private final Set<Field> cells;

    private final Set<Field> sections;

    public SectionValidator(Field sectionField) {
        cells = AnnotationUtil.getAnnotatedFields(sectionField.getType(), CellValidation.class);
        sections = AnnotationUtil.getAnnotatedFields(sectionField.getType(), Section.class);
    }

    @Override
    public List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext) {
        List<CellValidationMessage> errors = new ArrayList<>();
        Sheet sheet = currentInstance.getClass().getAnnotation(Sheet.class);
        validatorContext.setSheet(sheet);
        for (Field cell: cells) {
            ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(cell, ExcelSheetValidatorType.CELL);
            validatorContext.setField(cell);
            validatorUtil.setValidatorContext(validatorContext);
            errors.addAll(validatorUtil.validate(currentInstance, validatorContext));
        }
        for (Field section: sections) {
            ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(section, ExcelSheetValidatorType.SECTION);
            validatorUtil.setValidatorContext(validatorContext);
            Object sectionInstance = ReflectionUtil.getFieldValue(currentInstance, section);
            errors.addAll(validatorUtil.validate(sectionInstance, validatorContext));
        }
        return errors;
    }
}
