package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import java.util.ArrayList;
import java.util.List;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelValidatorUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

/**
 * @author Salil V Nair
 */
public class SheetValidator extends BaseExcelValidator {
    private final List<?> rows;
    public SheetValidator(List<?> rows) {
        this.rows = rows;
    }
    @Override
    public List<ValidationMessage> validate(Object currentInstance, ValidatorContext validatorContext) {
        List<ValidationMessage> errors = new ArrayList<>();
        for (Object row: rows) {
            ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(row, ExcelSheetValidatorType.ROW);
            validatorContext.setCurrentRow((BaseExcelSheet) row);
            errors.addAll(validatorUtil.validate(row, validatorContext));
        }
        return errors;
    }
}
