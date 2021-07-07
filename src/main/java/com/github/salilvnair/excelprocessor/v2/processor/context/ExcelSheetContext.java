package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
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
    private List<ValidationMessage> sheetValidationMessages;
    private Map<String, List<? extends BaseExcelSheet>> excelSheets;
    private Map<String, List<ValidationMessage>> excelValidationMessages;
    private ExcelSheetReaderContext readerContext;
    private Map<String, ExcelSheetReaderContext> readerContexts;

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

    public Map<String, List<ValidationMessage>> excelValidationMessages() {
        return excelValidationMessages;
    }

    public void setExcelValidationMessages(Map<String, List<ValidationMessage>> excelValidationMessages) {
        this.excelValidationMessages = excelValidationMessages;
    }

    public List<ValidationMessage> sheetValidationMessages() {
        return sheetValidationMessages;
    }

    public void setSheetValidationMessages(List<ValidationMessage> sheetValidationMessages) {
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
}
