package com.github.salilvnair.excelprocessor.v2.processor.validator.helper;

import java.util.ArrayList;
import java.util.List;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.IExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.factory.ExcelValidatorFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;


/**
 * @author Salil V Nair
 */
public class ExcelValidatorUtil {
    private List<IExcelValidator> excelValidators;

    private CellValidatorContext validatorContext;

    public ExcelValidatorUtil(CellValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    public ExcelValidatorUtil(Object object, ExcelSheetValidatorType sheetValidatorType) {
        this.setRowValidators(ExcelValidatorFactory.generate(object,sheetValidatorType));
    }

    public List<CellValidationMessage> validate(BaseSheet row) {
        this.init(row,ExcelSheetValidatorType.ROW);
        validatorContext.setCurrentRow(row);
        return this.validate(row,validatorContext);
    }

    public List<CellValidationMessage> validate(List<? extends BaseSheet> rows) {
        this.init(rows,ExcelSheetValidatorType.SHEET);
        validatorContext.setCurrentSheet(rows);
        return this.validate(rows,validatorContext);
    }

    public List<CellValidationMessage> validate(Object requestInstance, CellValidatorContext validatorContext) {
        List<CellValidationMessage> errors = new ArrayList<>();
        for (IExcelValidator v : excelValidators()) {
            errors.addAll(v.validate(requestInstance,validatorContext));
        }
        return errors;
    }

    public List<IExcelValidator> excelValidators() {
        return excelValidators;
    }

    public void setRowValidators(List<IExcelValidator> IExcelValidators) {
        this.excelValidators = IExcelValidators;
    }

    private void init(Object object,ExcelSheetValidatorType sheetValidatorType) {
        this.setRowValidators(ExcelValidatorFactory.generate(object,sheetValidatorType));
    }

    public CellValidatorContext validatorContext() {
        return validatorContext;
    }

    public void setValidatorContext(CellValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }
}
