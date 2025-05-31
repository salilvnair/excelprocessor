package com.github.salilvnair.excelprocessor.v2.processor.validator.context;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Setter
public class CellValidatorContext {
    private Sheet sheet;
    private String sheetName;
    private BaseSheet currentRow;
    @Getter
    private List<? extends BaseSheet> currentSheet;
    @Getter
    private Map<String, List<? extends BaseSheet>> excelSheets;
    @Getter
    private Field field;
    private String header;
    private String headerKey;
    private ExcelSheetContext sheetContext;
    private ExcelSheetReaderContext readerContext;
    private ExcelValidator validator;
    private Map<String,Object> userValidatorMap;
    private Map<String,List<String>> validValuesDataSet;
    private Map<String,String> userDefinedMessageDataSet;
    private Map<String, CellValidationInfo> headerKeyedCellValidationInfo;
    private AbstractExcelTaskValidator taskValidatorBean;
    private List<Object> taskMetadata;
    private Function<String, Object> beanResolver;


    public Map<String, Object> userValidatorMap() {
        if(userValidatorMap==null) {
            userValidatorMap = new HashMap<>();
        }
        return userValidatorMap;
    }

    public Map<String, List<String>> validValuesDataSet() {
        if(validValuesDataSet==null) {
            validValuesDataSet = new HashMap<>();
        }
        return validValuesDataSet;
    }

    public Map<String, String> userDefinedMessageDataSet() {
        if(userDefinedMessageDataSet==null) {
            userDefinedMessageDataSet = new HashMap<>();
        }
        return userDefinedMessageDataSet;
    }

    public Map<String, CellValidationInfo> headerKeyedCellValidationInfo() {
        if(headerKeyedCellValidationInfo==null) {
            headerKeyedCellValidationInfo = new HashMap<>();
        }
        return headerKeyedCellValidationInfo;
    }

    public Sheet sheet() {
        return sheet;
    }

    public ExcelValidator validator() {
        return validator;
    }

    public ExcelSheetReaderContext readerContext() {
        return readerContext;
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

    public String sheetName() {
        return sheetName;
    }

    public ExcelSheetContext sheetContext() {
        return sheetContext;
    }

    public AbstractExcelTaskValidator taskValidatorBean() {
        return taskValidatorBean;
    }

    public List<Object> taskMetadata() {
        return taskMetadata;
    }

    public String header() {
        return header;
    }

    public String headerKey() {
        return headerKey;
    }

    public Function<String, Object> beanResolver() {
        return beanResolver;
    }

}
