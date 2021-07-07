package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
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
    private List<? extends BaseExcelSheet> sheet;
    private List<CellValidationMessage> sheetValidationMessages;
    private Map<String, List<? extends BaseExcelSheet>> excelSheets;
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

    public File getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public Map<String, List<? extends BaseExcelSheet>> excelSheets() {
        return excelSheets;
    }

    public void setExcelSheets(Map<String, List<? extends BaseExcelSheet>> excelSheets) {
        this.excelSheets = excelSheets;
    }

    public List<? extends BaseExcelSheet> sheet() {
        return sheet;
    }

    public void setSheet(List<? extends BaseExcelSheet> sheet) {
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
