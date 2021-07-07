package com.github.salilvnair.excelprocessor.v2.processor.validator.helper;

import java.util.ArrayList;
import java.util.List;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.IExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.factory.ExcelValidatorFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;


/**
 * @author Salil V Nair
 */
public class ExcelValidatorUtil {
    private List<IExcelValidator> excelValidators;

    private ValidatorContext validatorContext;

    public ExcelValidatorUtil(ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    public ExcelValidatorUtil(Object object, ExcelSheetValidatorType sheetValidatorType) {
        this.setRowValidators(ExcelValidatorFactory.generate(object,sheetValidatorType));
    }

    public List<ValidationMessage> validate(BaseExcelSheet row) {
        this.init(row,ExcelSheetValidatorType.ROW);
        validatorContext.setCurrentRow(row);
        return this.validate(row,validatorContext);
    }

    public List<ValidationMessage> validate(List<? extends BaseExcelSheet> rows) {
        this.init(rows,ExcelSheetValidatorType.SHEET);
        validatorContext.setCurrentSheet(rows);
        return this.validate(rows,validatorContext);
    }

    public List<ValidationMessage> validate(Object requestInstance,ValidatorContext validatorContext) {
        List<ValidationMessage> errors = new ArrayList<>();
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

    public ValidatorContext validatorContext() {
        return validatorContext;
    }

    public void setValidatorContext(ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }
}
