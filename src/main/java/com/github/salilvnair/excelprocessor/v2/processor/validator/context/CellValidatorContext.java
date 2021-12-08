package com.github.salilvnair.excelprocessor.v2.processor.validator.context;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.IExcelValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellValidatorContext {
    private Sheet sheet;
    private String sheetName;
    private BaseSheet currentRow;
    private List<? extends BaseSheet> currentSheet;
    private Map<String, List<? extends BaseSheet>> excelSheets;
    private Field field;
    private ExcelSheetContext sheetContext;
    private ExcelSheetReaderContext readerContext;
    private IExcelValidator validator;
    private Map<String,Object> userValidatorMap;
    private Map<String,List<String>> validValuesDataSet;
    private Map<String,String> userDefinedMessageDataSet;


    public List<? extends BaseSheet> getCurrentSheet() {
        return currentSheet;
    }

    public void setCurrentSheet(List<? extends BaseSheet> currentSheet) {
        this.currentSheet = currentSheet;
    }

    public Map<String, List<? extends BaseSheet>> getExcelSheets() {
        return excelSheets;
    }

    public void setExcelSheets(Map<String, List<? extends BaseSheet>> excelSheets) {
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

    public Sheet sheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
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

    public BaseSheet currentRow() {
        return currentRow;
    }

    public <T> T sheet(Class<T> clazz) {
        if(clazz.isInstance(currentRow)) {
            return clazz.cast(currentRow);
        }
        return null;
    }

    public void setCurrentRow(BaseSheet currentRow) {
        this.currentRow = currentRow;
    }

    public String sheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public ExcelSheetContext sheetContext() {
        return sheetContext;
    }

    public void setSheetContext(ExcelSheetContext sheetContext) {
        this.sheetContext = sheetContext;
    }
}
