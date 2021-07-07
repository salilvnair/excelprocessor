package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.AbstractExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelValidatorUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Salil V Nair
 */
public class RowValidator extends AbstractExcelValidator {
    private Set<Field> columns = new HashSet<>();
    public RowValidator(Object rowInstance) {
        columns = AnnotationUtil.getAnnotatedFields(rowInstance.getClass(), CellValidation.class);
    }
    @Override
    public List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext) {
        List<CellValidationMessage> errors = new ArrayList<>();
        Sheet sheet = currentInstance.getClass().getAnnotation(Sheet.class);
        validatorContext.setSheet(sheet);
        for (Field column: columns) {
            ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(column, ExcelSheetValidatorType.COLUMN);
            validatorContext.setField(column);
            validatorUtil.setValidatorContext(validatorContext);
            errors.addAll(validatorUtil.validate(currentInstance, validatorContext));
        }
        return errors;
    }
}
