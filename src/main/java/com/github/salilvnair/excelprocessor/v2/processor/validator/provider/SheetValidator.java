package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import java.util.ArrayList;
import java.util.List;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.AbstractExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelValidatorUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
public class SheetValidator extends AbstractExcelValidator {
    private final List<?> rows;
    public SheetValidator(List<?> rows) {
        this.rows = rows;
    }
    @Override
    public List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext) {
        List<CellValidationMessage> errors = new ArrayList<>();
        for (Object row: rows) {
            ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(row, ExcelSheetValidatorType.ROW);
            validatorContext.setCurrentRow((BaseSheet) row);
            errors.addAll(validatorUtil.validate(row, validatorContext));
        }
        return errors;
    }
}
