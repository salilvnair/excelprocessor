package com.github.salilvnair.excelprocessor.v2.processor.validator.helper;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelValidator {
    private ExcelValidator(ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }
    private ValidatorContext validatorContext;
    private BaseExcelSheet row;
    private List<? extends BaseExcelSheet> rows;

    public static ExcelValidator init(ValidatorContext validatorContext) {
        return new ExcelValidator(validatorContext);
    }

    public ExcelValidator setUserValidatorMap(Map<String,Object> userValidatorMap) {
        initValidatorContext();
        validatorContext.setUserValidatorMap(userValidatorMap);
        return this;
    }

    public ExcelValidator setValidValuesDataSet(Map<String,List<String>> validValuesDataSet) {
        initValidatorContext();
        validatorContext.setValidValuesDataSet(validValuesDataSet);
        return this;
    }

    private void initValidatorContext() {
        if(this.validatorContext==null) {
            this.validatorContext = new ValidatorContext();
        }
    }

    public ExcelValidator setUserDefinedMessageDataSet(Map<String,String> userDefinedMessageDataSet) {
        initValidatorContext();
        validatorContext.setUserDefinedMessageDataSet(userDefinedMessageDataSet);
        return this;
    }

    public ExcelValidator row(BaseExcelSheet row) {
        this.row = row;
        return this;
    }


    public ExcelValidator rows(List<? extends BaseExcelSheet> rows) {
        this.rows = rows;
        return this;
    }

    public List<ValidationMessage> validate() {
        if(row==null && (this.rows==null||this.rows.isEmpty())) {
            return Collections.emptyList();
        }
        initValidatorContext();
        ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(validatorContext);
        if(this.rows==null||this.rows.isEmpty()) {
            return validatorUtil.validate(row);
        }
        else {
            return validatorUtil.validate(rows);
        }
    }
}
