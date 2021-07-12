package com.github.salilvnair.excelprocessor.v2.context;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetContext {
    private File excelFile;
    private String fileName;
    private Workbook workbook;
    private String sheetName;
    private List<String> ignoreHeaders;
    private List<Integer> ignoreHeaderRows;
    private List<String> ignoreHeaderColumns;
    private List<? extends BaseSheet> sheet;
    private List<CellValidationMessage> sheetValidationMessages;
    private Map<String, List<? extends BaseSheet>> excelSheets;
    private Map<String, List<CellValidationMessage>> excelValidationMessages;
    private ExcelSheetReaderContext readerContext;
    private Map<String, ExcelSheetReaderContext> readerContexts;
    private Map<String,Object> userValidatorMap;
    private Map<String,List<String>> validValuesDataSet;
    private Map<String,String> userDefinedMessageDataSet;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public File excelFile() {
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public Map<String, List<? extends BaseSheet>> excelSheets() {
        return excelSheets;
    }

    public void setExcelSheets(Map<String, List<? extends BaseSheet>> excelSheets) {
        this.excelSheets = excelSheets;
    }

    public List<? extends BaseSheet> sheet() {
        return sheet;
    }

    public void setSheet(List<? extends BaseSheet> sheet) {
        this.sheet = sheet;
    }

    public Map<String, List<CellValidationMessage>> excelValidationMessages() {
        return excelValidationMessages;
    }

    public void setExcelValidationMessages(Map<String, List<CellValidationMessage>> excelValidationMessages) {
        this.excelValidationMessages = excelValidationMessages;
    }

    public List<CellValidationMessage> sheetValidationMessages() {
        return sheetValidationMessages;
    }

    public void setSheetValidationMessages(List<CellValidationMessage> sheetValidationMessages) {
        this.sheetValidationMessages = sheetValidationMessages;
    }

    public ExcelSheetReaderContext readerContext() {
        return readerContext;
    }

    public void setReaderContext(ExcelSheetReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    public Map<String, ExcelSheetReaderContext> readerContexts() {
        if(readerContexts == null) {
            readerContexts = new HashMap<>();
        }
        return readerContexts;
    }

    public void setReaderContexts(Map<String, ExcelSheetReaderContext> readerContexts) {
        this.readerContexts = readerContexts;
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

    public static ExcelSheetContextBuilder builder() {
        return new ExcelSheetContextBuilder();
    }

    public String sheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public List<String> ignoreHeaders() {
        if(ignoreHeaders == null) {
            ignoreHeaders = new ArrayList<>();
        }
        return ignoreHeaders;
    }

    public void setIgnoreHeaders(List<String> ignoreHeaders) {
        this.ignoreHeaders = ignoreHeaders;
    }

    public List<Integer> ignoreHeaderRows() {
        return ignoreHeaderRows;
    }

    public void setIgnoreHeaderRows(List<Integer> ignoreHeaderRows) {
        this.ignoreHeaderRows = ignoreHeaderRows;
    }

    public List<String> ignoreHeaderColumns() {
        return ignoreHeaderColumns;
    }

    public void setIgnoreHeaderColumns(List<String> ignoreHeaderColumns) {
        this.ignoreHeaderColumns = ignoreHeaderColumns;
    }

    public static class ExcelSheetContextBuilder {
        private ExcelSheetContext excelSheetContext =  new ExcelSheetContext();
        public ExcelSheetContextBuilder fileName(String fileName) {
            excelSheetContext.setFileName(fileName);
            return this;
        }

        public ExcelSheetContextBuilder workbook(Workbook workbook) {
            excelSheetContext.setWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder excelFile(File excelFile) {
            excelSheetContext.setExcelFile(excelFile);
            return this;
        }

        public ExcelSheetContextBuilder sheetName(String sheetName) {
            excelSheetContext.setSheetName(sheetName);
            return this;
        }

        public ExcelSheetContextBuilder ignoreHeaders(List<String> ignoreHeaders) {
            excelSheetContext.setIgnoreHeaders(ignoreHeaders);
            return this;
        }

        public ExcelSheetContextBuilder ignoreHeader(String ignoreHeader) {
            excelSheetContext.ignoreHeaders.add(ignoreHeader);
            return this;
        }

        public ExcelSheetContextBuilder ignoreHeaderRows(List<Integer> rows) {
            excelSheetContext.setIgnoreHeaderRows(rows);
            return this;
        }

        public ExcelSheetContextBuilder ignoreHeaderColumns(List<String> columns) {
            excelSheetContext.setIgnoreHeaderColumns(columns);
            return this;
        }


        public ExcelSheetContextBuilder userValidatorMap(Map<String,Object> userValidatorMap) {
            excelSheetContext.setUserValidatorMap(userValidatorMap);
            return this;
        }
        public ExcelSheetContextBuilder validValuesDataSet(Map<String,List<String>> validValuesDataSet) {
            excelSheetContext.setValidValuesDataSet(validValuesDataSet);
            return this;
        }
        public ExcelSheetContextBuilder userDefinedMessageDataSet(Map<String,String> userDefinedMessageDataSet) {
            excelSheetContext.setUserDefinedMessageDataSet(userDefinedMessageDataSet);
            return this;
        }

        public ExcelSheetContext build() {
            return excelSheetContext;
        }

    }
}
