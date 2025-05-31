package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.model.FieldInfo;
import com.github.salilvnair.excelprocessor.v2.processor.provider.core.BaseExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
@Setter
@Getter
public class ExcelSheetReaderContext extends BaseExcelSheetContext {
    @Setter
    private Sheet sheet;
    @Setter
    private String sheetName;
    @Setter
    private List<String> ignoreHeaders;
    @Setter
    private List<String> ignoreHeaderPatterns;
    @Setter
    private List<Integer> ignoreHeaderRows;
    @Setter
    private List<String> ignoreHeaderColumns;
    @Setter
    private Map<Cell, Field> headerCellFieldMap;
    private Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap;
    private Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap;
    private Map<Integer, String> headerColumnIndexKeyedHeaderValueMap;
    private Map<Integer, String> headerRowIndexKeyedHeaderValueMap;
    @Setter
    @Getter
    private List<? extends BaseSheet> sheetData;
    private List<BaseSheet> concurrentSheetData;
    @Setter
    private boolean extractMultiOrientedMap;
    @Setter
    private Map<String, List<? extends BaseSheet>> multiOrientedSheetMap;
    @Setter
    private Map<String, List<? extends BaseSheet>> scatteredSheetMap;
    @Setter
    private Map<String, FieldInfo> headerFieldInfoMap;
    private Map<String, ExcelSheetReaderContext> multiOrientedReaderContexts;
    @Setter
    private String valueColumnBeginsAt;
    @Setter
    private String valueColumnEndsAt;
    @Setter
    private int valueRowBeginsAt=-1;
    @Setter
    private int valueRowEndsAt=-1;
    @Setter
    private boolean suppressExceptions;
    @Setter
    private boolean suppressTaskExceptions;

    public Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap() {
        if(rowIndexKeyedHeaderKeyCellInfoMap == null) {
            rowIndexKeyedHeaderKeyCellInfoMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return rowIndexKeyedHeaderKeyCellInfoMap;
    }

    public Map<Integer, String> headerColumnIndexKeyedHeaderValueMap() {
        if(headerColumnIndexKeyedHeaderValueMap == null) {
            headerColumnIndexKeyedHeaderValueMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return headerColumnIndexKeyedHeaderValueMap;
    }

    public Sheet sheet() {
        return sheet;
    }

    public Map<Integer, String> headerRowIndexKeyedHeaderValueMap() {
        if(headerRowIndexKeyedHeaderValueMap == null) {
            headerRowIndexKeyedHeaderValueMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return headerRowIndexKeyedHeaderValueMap;
    }

    public Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap() {
        if(colIndexKeyedHeaderKeyCellInfoMap == null) {
            colIndexKeyedHeaderKeyCellInfoMap = BaseExcelProcessor.orderedOrUnorderedMap(sheet);
        }
        return colIndexKeyedHeaderKeyCellInfoMap;
    }

    public Map<String, List<? extends BaseSheet>> multiOrientedSheetMap() {
        return multiOrientedSheetMap;
    }

    public boolean extractMultiOrientedMap() {
        return extractMultiOrientedMap;
    }

    public String valueColumnBeginsAt() {
        return valueColumnBeginsAt;
    }

    public String valueColumnEndsAt() {
        return valueColumnEndsAt;
    }

    public int valueRowBeginsAt() {
        return valueRowBeginsAt;
    }

    public int valueRowEndsAt() {
        return valueRowEndsAt;
    }

    public String sheetName() {
        return sheetName;
    }

    public Map<String, List<? extends BaseSheet>> scatteredSheetMap() {
        return scatteredSheetMap;
    }

    public Map<String, FieldInfo> headerFieldInfoMap() {
        return headerFieldInfoMap;
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

    public List<String> ignoreHeaders() {
        if(ignoreHeaders == null) {
            ignoreHeaders = new ArrayList<>();
        }
        return ignoreHeaders;
    }

    public List<Integer> ignoreHeaderRows() {
        if(ignoreHeaderRows == null) {
            ignoreHeaderRows = new ArrayList<>();
        }
        return ignoreHeaderRows;
    }

    public List<String> ignoreHeaderPatterns() {
        if(ignoreHeaderPatterns == null) {
            ignoreHeaderPatterns = new ArrayList<>();
        }
        return ignoreHeaderPatterns;
    }

    public List<String> ignoreHeaderColumns() {
        if(ignoreHeaderColumns == null) {
            ignoreHeaderColumns = new ArrayList<>();
        }
        return ignoreHeaderColumns;
    }

    public List<BaseSheet> concurrentSheetData() {
        if(concurrentSheetData == null) {
            concurrentSheetData = BaseExcelProcessor.orderedOrUnorderedList(sheet);
        }
        return concurrentSheetData;
    }

    public boolean suppressExceptions() {
        return suppressExceptions;
    }

    public boolean suppressTaskExceptions() {
        return suppressTaskExceptions;
    }

}
