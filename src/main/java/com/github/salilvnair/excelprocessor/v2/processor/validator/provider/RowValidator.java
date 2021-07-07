package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseExcelValidator;
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
public class RowValidator extends BaseExcelValidator {
    private Set<Field> columns = new HashSet<>();
    public RowValidator(Object rowInstance) {
        columns = AnnotationUtil.getAnnotatedFields(rowInstance.getClass(), ExcelHeaderValidator.class);
    }
    @Override
    public List<ValidationMessage> validate(Object currentInstance, ValidatorContext validatorContext) {
        List<ValidationMessage> errors = new ArrayList<>();
        ExcelSheet excelSheet = currentInstance.getClass().getAnnotation(ExcelSheet.class);
        validatorContext.setExcelSheet(excelSheet);
        for (Field column: columns) {
            ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(column, ExcelSheetValidatorType.COLUMN);
            validatorContext.setField(column);
            validatorUtil.setValidatorContext(validatorContext);
            errors.addAll(validatorUtil.validate(currentInstance, validatorContext));
        }
        return errors;
    }
}
