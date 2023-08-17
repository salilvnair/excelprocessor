package com.github.salilvnair.excelprocessor.v2.context;

import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author Salil V Nair
 */
public class ExcelSheetContext {
    private File excelFile;
    private String fileName;
    private String filePath;
    private Workbook workbook;
    private Workbook styleTemplateWorkbook;
    private File template;
    private String sheetName;
    private List<String> ignoreHeaders;
    private List<Integer> ignoreHeaderRows;
    private List<String> ignoreHeaderColumns;
    private Set<String> orderedHeaders;
    private Map<String, String> dynamicHeaderDisplayNames;
    private Map<String, DataCellStyleInfo> dynamicHeaderDataCellStyleInfo;
    private Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo;
    private List<? extends BaseSheet> sheet;
    private List<CellValidationMessage> sheetValidationMessages;
    private Map<String, List<? extends BaseSheet>> excelSheets;
    private Map<String, List<CellValidationMessage>> excelValidationMessages;
    private ExcelSheetReaderContext readerContext;
    private ExcelSheetWriterContext writerContext;
    private Map<String, ExcelSheetReaderContext> readerContexts;
    private Map<String,Object> userValidatorMap;
    private Map<String,List<String>> validValuesDataSet;
    private Map<String,String> userDefinedMessageDataSet;
    private boolean suppressExceptions;

    public String fileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Workbook workbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public Workbook styleTemplateWorkbook() {
        return styleTemplateWorkbook;
    }

    public void setStyleTemplateWorkbook(Workbook styleTemplateWorkbook) {
        this.styleTemplateWorkbook = styleTemplateWorkbook;
    }

    public File excelFile() {
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public File template() {
        return template;
    }

    public void setTemplate(File template) {
        this.template = template;
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

    public ExcelSheetWriterContext writerContext() {
        if(writerContext == null) {
            writerContext = new ExcelSheetWriterContext();
        }
        return writerContext;
    }

    public void setWriterContext(ExcelSheetWriterContext writerContext) {
        this.writerContext = writerContext;
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

    public Set<String> orderedHeaders() {
        if(orderedHeaders == null) {
            orderedHeaders = new HashSet<>();
        }
        return orderedHeaders;
    }

    public void setOrderedHeaders(Set<String> orderedHeaders) {
        this.orderedHeaders = orderedHeaders;
    }

    public Map<String, DataCellStyleInfo> dynamicHeaderDataCellStyleInfo() {
        if(dynamicHeaderDataCellStyleInfo == null) {
            dynamicHeaderDataCellStyleInfo = new HashMap<>();
        }
        return dynamicHeaderDataCellStyleInfo;
    }

    public void setDynamicHeaderDataCellStyleInfo(Map<String, DataCellStyleInfo> dynamicHeaderDataCellStyleInfo) {
        this.dynamicHeaderDataCellStyleInfo = dynamicHeaderDataCellStyleInfo;
    }

    public Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo() {
        if(dynamicHeaderCellStyleInfo == null) {
            dynamicHeaderCellStyleInfo = new HashMap<>();
        }
        return dynamicHeaderCellStyleInfo;
    }

    public void setDynamicHeaderCellStyleInfo(Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo) {
        this.dynamicHeaderCellStyleInfo = dynamicHeaderCellStyleInfo;
    }

    public Map<String, String> dynamicHeaderDisplayNames() {
        if(dynamicHeaderDisplayNames == null) {
            dynamicHeaderDisplayNames = new HashMap<>();
        }
        return dynamicHeaderDisplayNames;
    }

    public void setDynamicHeaderDisplayNames(Map<String, String> dynamicHeaderDisplayNames) {
        this.dynamicHeaderDisplayNames = dynamicHeaderDisplayNames;
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

    public boolean suppressExceptions() {
        return suppressExceptions;
    }

    public void setSuppressExceptions(boolean suppressExceptions) {
        this.suppressExceptions = suppressExceptions;
    }

    public String filePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static class ExcelSheetContextBuilder {
        private final ExcelSheetContext excelSheetContext =  new ExcelSheetContext();
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

        public ExcelSheetContextBuilder filePath(String filePath) {
            excelSheetContext.setFilePath(filePath);
            return this;
        }

        public ExcelSheetContextBuilder template(File templateFile) {
            Workbook workbook = null;
            try {
                FileInputStream inputS = new FileInputStream(templateFile);
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputS, templateFile.getAbsolutePath());

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setTemplate(templateFile);
            excelSheetContext.setWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder dynamicHeaderCellStyleInfo(Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo) {
            excelSheetContext.setDynamicHeaderCellStyleInfo(dynamicHeaderCellStyleInfo);
            return this;
        }

        public ExcelSheetContextBuilder dynamicHeaderDataCellStyleInfo(Map<String, DataCellStyleInfo> dynamicHeaderDataCellStyleInfo) {
            excelSheetContext.setDynamicHeaderDataCellStyleInfo(dynamicHeaderDataCellStyleInfo);
            return this;
        }

        public ExcelSheetContextBuilder styleTemplate(File styleTemplate) {
            Workbook workbook = null;
            try {
                FileInputStream inputS = new FileInputStream(styleTemplate);
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputS, styleTemplate.getAbsolutePath());

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setStyleTemplateWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder template(InputStream inputStream, String fileNameWithExtension) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputStream, fileNameWithExtension);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder styleTemplate(InputStream inputStream, String fileNameWithExtension) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputStream, fileNameWithExtension);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setStyleTemplateWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder template(InputStream inputStream, ExcelFileType excelFileType) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputStream, excelFileType);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder styleTemplate(InputStream inputStream, ExcelFileType excelFileType) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputStream, excelFileType);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setStyleTemplateWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder template(byte[] bytes, String fileNameWithExtension) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(new ByteArrayInputStream(bytes), fileNameWithExtension);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder styleTemplate(byte[] bytes, String fileNameWithExtension) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(new ByteArrayInputStream(bytes), fileNameWithExtension);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setStyleTemplateWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder template(byte[] bytes, ExcelFileType excelFileType) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(new ByteArrayInputStream(bytes), excelFileType);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setWorkbook(workbook);
            return this;
        }

        public ExcelSheetContextBuilder styleTemplate(byte[] bytes, ExcelFileType excelFileType) {
            Workbook workbook = null;
            try {
                workbook = ExcelSheetWriterUtil.generateWorkbook(new ByteArrayInputStream(bytes), excelFileType);

            }
            catch (Exception e) {
                if(!excelSheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
            excelSheetContext.setStyleTemplateWorkbook(workbook);
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
            excelSheetContext.ignoreHeaders().add(ignoreHeader);
            return this;
        }

        public ExcelSheetContextBuilder ignoreHeaderRows(List<Integer> rows) {
            excelSheetContext.setIgnoreHeaderRows(rows);
            return this;
        }

        public ExcelSheetContextBuilder orderedHeaders(Set<String> orderedHeaders) {
            excelSheetContext.setOrderedHeaders(orderedHeaders);
            return this;
        }

        public ExcelSheetContextBuilder dynamicHeaderDisplayNames(Map<String, String> dynamicHeaderDisplayNames) {
            excelSheetContext.setDynamicHeaderDisplayNames(dynamicHeaderDisplayNames);
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

        public ExcelSheetContextBuilder suppressExceptions() {
            excelSheetContext.setSuppressExceptions(true);
            return this;
        }

        public ExcelSheetContext build() {
            return excelSheetContext;
        }

    }
}
