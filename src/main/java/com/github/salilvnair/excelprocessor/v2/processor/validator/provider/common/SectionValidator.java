package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.common;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.AbstractExcelValidator;

import java.lang.reflect.Field;
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
        return validate(currentInstance, validatorContext, cells, sections);
    }
}
