package com.github.salilvnair.excelprocessor.v2.processor.validator.helper;

import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Salil V Nair
 */
public final class ExcelSheetValidator {

    private CellValidatorContext validatorContext;

    private BaseSheet row;

    private List<? extends BaseSheet> rows;

    private ExcelSheetValidator(CellValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    public static ExcelSheetValidator init(CellValidatorContext validatorContext) {
        return new ExcelSheetValidator(validatorContext);
    }

    public ExcelSheetValidator userValidatorMap(Map<String,Object> userValidatorMap) {
        initValidatorContext();
        validatorContext.setUserValidatorMap(userValidatorMap);
        return this;
    }

    public ExcelSheetValidator validValuesDataSet(Map<String,List<String>> validValuesDataSet) {
        initValidatorContext();
        validatorContext.setValidValuesDataSet(validValuesDataSet);
        return this;
    }

    private void initValidatorContext() {
        if(this.validatorContext==null) {
            this.validatorContext = new CellValidatorContext();
        }
    }

    public ExcelSheetValidator userDefinedMessageDataSet(Map<String,String> userDefinedMessageDataSet) {
        initValidatorContext();
        validatorContext.setUserDefinedMessageDataSet(userDefinedMessageDataSet);
        return this;
    }

    public ExcelSheetValidator headerKeyedCellValidationInfo(Map<String, CellValidationInfo> headerKeyedCellValidationInfo) {
        initValidatorContext();
        validatorContext.setHeaderKeyedCellValidationInfo(headerKeyedCellValidationInfo);
        return this;
    }

    public ExcelSheetValidator row(BaseSheet row) {
        this.row = row;
        return this;
    }


    public ExcelSheetValidator rows(List<? extends BaseSheet> rows) {
        this.rows = rows;
        return this;
    }

    public ExcelSheetValidator multiOriented(String sheetName) {
        initValidatorContext();
        validatorContext.setSheetName(sheetName);
        return this;
    }

    public ExcelSheetValidator taskValidatorBean(AbstractExcelTaskValidator abstractExcelTaskValidator) {
        initValidatorContext();
        validatorContext.setTaskValidatorBean(abstractExcelTaskValidator);
        return this;
    }

    public ExcelSheetValidator beanResolver(Function<String, Object> beanResolver) {
        initValidatorContext();
        validatorContext.setBeanResolver(beanResolver);
        return this;
    }

    public ExcelSheetValidator taskMetadata(List<Object> taskMetadata) {
        initValidatorContext();
        validatorContext.setTaskMetadata(taskMetadata);
        return this;
    }

    public List<CellValidationMessage> validate() {
        if(row==null && (this.rows==null||this.rows.isEmpty())) {
            return Collections.emptyList();
        }
        initValidatorContext();
        ExcelSheetValidatorUtil validatorUtil = new ExcelSheetValidatorUtil(validatorContext);
        if(this.rows==null || this.rows.isEmpty()) {
            return validatorUtil.validate(row);
        }
        else {
            return validatorUtil.validate(rows);
        }
    }
}
