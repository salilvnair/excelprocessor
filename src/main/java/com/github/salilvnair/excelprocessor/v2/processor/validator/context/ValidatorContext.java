package com.github.salilvnair.excelprocessor.v2.processor.validator.context;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.IExcelValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidatorContext {
    private ExcelSheet excelSheet;
    private BaseExcelSheet currentRow;
    private List<? extends BaseExcelSheet> currentSheet;
    private Map<String, List<? extends BaseExcelSheet>> excelSheets;
    private Field field;
    private ExcelSheetReaderContext readerContext;
    private IExcelValidator validator;
    private Map<String,Object> userValidatorMap;
    private Map<String,List<String>> validValuesDataSet;
    private Map<String,String> userDefinedMessageDataSet;


    public List<? extends BaseExcelSheet> getCurrentSheet() {
        return currentSheet;
    }

    public void setCurrentSheet(List<? extends BaseExcelSheet> currentSheet) {
        this.currentSheet = currentSheet;
    }

    public Map<String, List<? extends BaseExcelSheet>> getExcelSheets() {
        return excelSheets;
    }

    public void setExcelSheets(Map<String, List<? extends BaseExcelSheet>> excelSheets) {
        this.excelSheets = excelSheets;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Map<String, Object> userValidatorMap() {
        if(userValidatorMap==null) {
            userValidatorMap = new HashMap<>();
        }
        return userValidatorMap;
    }

    public void setUserValidatorMap(Map<String, Object> userValidatorMap) {
        this.userValidatorMap = userValidatorMap;
    }

    public Map<String, List<String>> validValuesDataSet() {
        if(validValuesDataSet==null) {
            validValuesDataSet = new HashMap<>();
        }
        return validValuesDataSet;
    }

    public void setValidValuesDataSet(Map<String, List<String>> validValuesDataSet) {
        this.validValuesDataSet = validValuesDataSet;
    }

    public Map<String, String> userDefinedMessageDataSet() {
        if(userDefinedMessageDataSet==null) {
            userDefinedMessageDataSet = new HashMap<>();
        }
        return userDefinedMessageDataSet;
    }

    public void setUserDefinedMessageDataSet(Map<String, String> userDefinedMessageDataSet) {
        this.userDefinedMessageDataSet = userDefinedMessageDataSet;
    }

    public ExcelSheet excelSheet() {
        return excelSheet;
    }

    public void setExcelSheet(ExcelSheet excelSheet) {
        this.excelSheet = excelSheet;
    }

    public IExcelValidator validator() {
        return validator;
    }

    public void setValidator(IExcelValidator validator) {
        this.validator = validator;
    }

    public ExcelSheetReaderContext readerContext() {
        return readerContext;
    }

    public void setReaderContext(ExcelSheetReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    public BaseExcelSheet currentRow() {
        return currentRow;
    }

    public <T> T sheet(Class<T> clazz) {
        if(clazz.isInstance(currentRow)) {
            return clazz.cast(currentRow);
        }
        return null;
    }

    public void setCurrentRow(BaseExcelSheet currentRow) {
        this.currentRow = currentRow;
    }
}
