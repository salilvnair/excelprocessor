package com.github.salilvnair.excelprocessor.v2.processor.validator.helper;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public final class ExcelValidator {

    private CellValidatorContext validatorContext;

    private BaseSheet row;

    private List<? extends BaseSheet> rows;

    private ExcelValidator(CellValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    public static ExcelValidator init(CellValidatorContext validatorContext) {
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
            this.validatorContext = new CellValidatorContext();
        }
    }

    public ExcelValidator setUserDefinedMessageDataSet(Map<String,String> userDefinedMessageDataSet) {
        initValidatorContext();
        validatorContext.setUserDefinedMessageDataSet(userDefinedMessageDataSet);
        return this;
    }

    public ExcelValidator row(BaseSheet row) {
        this.row = row;
        return this;
    }


    public ExcelValidator rows(List<? extends BaseSheet> rows) {
        this.rows = rows;
        return this;
    }

    public ExcelValidator multiOriented(String sheetName) {
        validatorContext.setSheetName(sheetName);
        return this;
    }

    public List<CellValidationMessage> validate() {
        if(row==null && (this.rows==null||this.rows.isEmpty())) {
            return Collections.emptyList();
        }
        initValidatorContext();
        ExcelValidatorUtil validatorUtil = new ExcelValidatorUtil(validatorContext);
        if(this.rows==null || this.rows.isEmpty()) {
            return validatorUtil.validate(row);
        }
        else {
            return validatorUtil.validate(rows);
        }
    }
}
