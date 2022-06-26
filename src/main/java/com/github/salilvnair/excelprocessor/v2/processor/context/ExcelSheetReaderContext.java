package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.provider.core.BaseExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderContext extends BaseExcelSheetContext {
    private Sheet sheet;
    private String sheetName;
    private List<String> ignoreHeaders;
    private List<String> ignoreHeaderPatterns;
    private List<Integer> ignoreHeaderRows;
    private List<String> ignoreHeaderColumns;
    private Map<Cell, Field> headerCellFieldMap;
    private Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap;
    private Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap;
    private Map<Integer, String> headerColumnIndexKeyedHeaderValueMap;
    private Map<Integer, String> headerRowIndexKeyedHeaderValueMap;
    private List<? extends BaseSheet> sheetData;
    private List<BaseSheet> concurrentSheetData;
    private boolean extractMultiOrientedMap;
    private Map<String, List<? extends BaseSheet>> multiOrientedSheetMap;
    private Map<String, List<? extends BaseSheet>> scatteredSheetMap;
    private Map<String, ExcelSheetReaderContext> multiOrientedReaderContexts;
    private String valueColumnBeginsAt;
    private String valueColumnEndsAt;
    private int valueRowBeginsAt=-1;
    private int valueRowEndsAt=-1;
    private boolean suppressExceptions;

    public Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap() {
        if(rowIndexKeyedHeaderKeyCellInfoMap == null) {
            rowIndexKeyedHeaderKeyCellInfoMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return rowIndexKeyedHeaderKeyCellInfoMap;
    }

    public void setRowIndexKeyedHeaderKeyCellInfoMap(Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap) {
        this.rowIndexKeyedHeaderKeyCellInfoMap = rowIndexKeyedHeaderKeyCellInfoMap;
    }

    public Map<Integer, String> headerColumnIndexKeyedHeaderValueMap() {
        if(headerColumnIndexKeyedHeaderValueMap == null) {
            headerColumnIndexKeyedHeaderValueMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return headerColumnIndexKeyedHeaderValueMap;
    }

    public void setHeaderColumnIndexKeyedHeaderValueMap(Map<Integer, String> headerColumnIndexKeyedHeaderValueMap) {
        this.headerColumnIndexKeyedHeaderValueMap = headerColumnIndexKeyedHeaderValueMap;
    }

    public Sheet sheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public List<? extends BaseSheet> getSheetData() {
        return sheetData;
    }

    public void setSheetData(List<? extends BaseSheet> sheetData) {
        this.sheetData = sheetData;
    }

    public Map<Integer, String> headerRowIndexKeyedHeaderValueMap() {
        if(headerRowIndexKeyedHeaderValueMap == null) {
            headerRowIndexKeyedHeaderValueMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return headerRowIndexKeyedHeaderValueMap;
    }

    public void setHeaderRowIndexKeyedHeaderValueMap(Map<Integer, String> headerRowIndexKeyedHeaderValueMap) {
        this.headerRowIndexKeyedHeaderValueMap = headerRowIndexKeyedHeaderValueMap;
    }

    public Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap() {
        if(colIndexKeyedHeaderKeyCellInfoMap == null) {
            colIndexKeyedHeaderKeyCellInfoMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return colIndexKeyedHeaderKeyCellInfoMap;
    }

    public void setColIndexKeyedHeaderKeyCellInfoMap(Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap) {
        this.colIndexKeyedHeaderKeyCellInfoMap = colIndexKeyedHeaderKeyCellInfoMap;
    }

    public Map<String, List<? extends BaseSheet>> multiOrientedSheetMap() {
        return multiOrientedSheetMap;
    }

    public void setMultiOrientedSheetMap(Map<String, List<? extends BaseSheet>> multiOrientedSheetMap) {
        this.multiOrientedSheetMap = multiOrientedSheetMap;
    }

    public boolean extractMultiOrientedMap() {
        return extractMultiOrientedMap;
    }

    public void setExtractMultiOrientedMap(boolean extractMultiOrientedMap) {
        this.extractMultiOrientedMap = extractMultiOrientedMap;
    }

    public String valueColumnBeginsAt() {
        return valueColumnBeginsAt;
    }

    public void setValueColumnBeginsAt(String valueColumnBeginsAt) {
        this.valueColumnBeginsAt = valueColumnBeginsAt;
    }

    public String valueColumnEndsAt() {
        return valueColumnEndsAt;
    }

    public void setValueColumnEndsAt(String valueColumnEndsAt) {
        this.valueColumnEndsAt = valueColumnEndsAt;
    }

    public int valueRowBeginsAt() {
        return valueRowBeginsAt;
    }

    public void setValueRowBeginsAt(int valueRowBeginsAt) {
        this.valueRowBeginsAt = valueRowBeginsAt;
    }

    public int valueRowEndsAt() {
        return valueRowEndsAt;
    }

    public void setValueRowEndsAt(int valueRowEndsAt) {
        this.valueRowEndsAt = valueRowEndsAt;
    }

    public String sheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Map<String, List<? extends BaseSheet>> scatteredSheetMap() {
        return scatteredSheetMap;
    }

    public void setScatteredSheetMap(Map<String, List<? extends BaseSheet>> scatteredSheetMap) {
        this.scatteredSheetMap = scatteredSheetMap;
    }

    public Map<String, ExcelSheetReaderContext> multiOrientedReaderContexts() {
        if(multiOrientedReaderContexts == null) {
            multiOrientedReaderContexts =  new HashMap<>();
        }
        return multiOrientedReaderContexts;
    }

    public Map<Cell, Field> headerCellFieldMap() {
        return headerCellFieldMap;
    }

    public void setHeaderCellFieldMap(Map<Cell, Field> headerCellFieldMap) {
        this.headerCellFieldMap = headerCellFieldMap;
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
        if(ignoreHeaderRows == null) {
            ignoreHeaderRows = new ArrayList<>();
        }
        return ignoreHeaderRows;
    }

    public void setIgnoreHeaderRows(List<Integer> ignoreHeaderRows) {
        this.ignoreHeaderRows = ignoreHeaderRows;
    }

    public List<String> ignoreHeaderPatterns() {
        if(ignoreHeaderPatterns == null) {
            ignoreHeaderPatterns = new ArrayList<>();
        }
        return ignoreHeaderPatterns;
    }

    public void setIgnoreHeaderPatterns(List<String> ignoreHeaderPatterns) {
        this.ignoreHeaderPatterns = ignoreHeaderPatterns;
    }

    public List<String> ignoreHeaderColumns() {
        if(ignoreHeaderColumns == null) {
            ignoreHeaderColumns = new ArrayList<>();
        }
        return ignoreHeaderColumns;
    }

    public void setIgnoreHeaderColumns(List<String> ignoreHeaderColumns) {
        this.ignoreHeaderColumns = ignoreHeaderColumns;
    }

    public List<BaseSheet> concurrentSheetData() {
        if(concurrentSheetData == null) {
            concurrentSheetData = BaseExcelProcessor.orderedOrUnorderedList(sheet);
        }
        return concurrentSheetData;
    }

    public void setConcurrentSheetData(List<BaseSheet> concurrentSheetData) {
        this.concurrentSheetData = concurrentSheetData;
    }

    public boolean suppressExceptions() {
        return suppressExceptions;
    }

    public void setSuppressExceptions(boolean suppressExceptions) {
        this.suppressExceptions = suppressExceptions;
    }
}
