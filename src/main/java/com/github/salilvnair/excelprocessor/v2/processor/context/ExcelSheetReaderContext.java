package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderContext {
    private InputStream excelFileInputStream;
    private String fileName;
    private Workbook workbook;
    private Sheet sheet;
    private String sheetName;
    private Map<String, Field> headerKeyFieldMap;
    private Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap;
    private Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap;
    private Map<Integer, String> headerColumnIndexKeyedHeaderValueMap;
    private Map<Integer, String> headerRowIndexKeyedHeaderValueMap;
    private List<? extends BaseExcelSheet> sheetData;
    private boolean extractMultiOrientedMap;
    private Map<String, List<? extends BaseExcelSheet>> multiOrientedSheetMap;
    private Map<String, List<? extends BaseExcelSheet>> scatteredSheetMap;
    private Map<String, ExcelSheetReaderContext> multiOrientedReaderContexts;


    public Map<String, Field> getHeaderKeyFieldMap() {
        return headerKeyFieldMap;
    }

    public void setHeaderKeyFieldMap(Map<String, Field> headerKeyFieldMap) {
        this.headerKeyFieldMap = headerKeyFieldMap;
    }

    public Map<Integer, Map<String, CellInfo>> getRowIndexKeyedHeaderKeyCellInfoMap() {
        return rowIndexKeyedHeaderKeyCellInfoMap;
    }

    public void setRowIndexKeyedHeaderKeyCellInfoMap(Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap) {
        this.rowIndexKeyedHeaderKeyCellInfoMap = rowIndexKeyedHeaderKeyCellInfoMap;
    }

    public Map<Integer, String> getHeaderColumnIndexKeyedHeaderValueMap() {
        return headerColumnIndexKeyedHeaderValueMap;
    }

    public void setHeaderColumnIndexKeyedHeaderValueMap(Map<Integer, String> headerColumnIndexKeyedHeaderValueMap) {
        this.headerColumnIndexKeyedHeaderValueMap = headerColumnIndexKeyedHeaderValueMap;
    }

    public InputStream getExcelFileInputStream() {
        return excelFileInputStream;
    }

    public void setExcelFileInputStream(InputStream excelFileInputStream) {
        this.excelFileInputStream = excelFileInputStream;
    }

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

    public Sheet sheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public List<? extends BaseExcelSheet> getSheetData() {
        return sheetData;
    }

    public void setSheetData(List<? extends BaseExcelSheet> sheetData) {
        this.sheetData = sheetData;
    }

    public Map<Integer, String> getHeaderRowIndexKeyedHeaderValueMap() {
        return headerRowIndexKeyedHeaderValueMap;
    }

    public void setHeaderRowIndexKeyedHeaderValueMap(Map<Integer, String> headerRowIndexKeyedHeaderValueMap) {
        this.headerRowIndexKeyedHeaderValueMap = headerRowIndexKeyedHeaderValueMap;
    }

    public Map<Integer, Map<String, CellInfo>> getColIndexKeyedHeaderKeyCellInfoMap() {
        return colIndexKeyedHeaderKeyCellInfoMap;
    }

    public void setColIndexKeyedHeaderKeyCellInfoMap(Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap) {
        this.colIndexKeyedHeaderKeyCellInfoMap = colIndexKeyedHeaderKeyCellInfoMap;
    }

    public Map<String, List<? extends BaseExcelSheet>> getMultiOrientedSheetMap() {
        return multiOrientedSheetMap;
    }

    public void setMultiOrientedSheetMap(Map<String, List<? extends BaseExcelSheet>> multiOrientedSheetMap) {
        this.multiOrientedSheetMap = multiOrientedSheetMap;
    }

    public boolean extractMultiOrientedMap() {
        return extractMultiOrientedMap;
    }

    public void setExtractMultiOrientedMap(boolean extractMultiOrientedMap) {
        this.extractMultiOrientedMap = extractMultiOrientedMap;
    }

    public String sheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Map<String, List<? extends BaseExcelSheet>> getScatteredSheetMap() {
        return scatteredSheetMap;
    }

    public void setScatteredSheetMap(Map<String, List<? extends BaseExcelSheet>> scatteredSheetMap) {
        this.scatteredSheetMap = scatteredSheetMap;
    }

    public Map<String, ExcelSheetReaderContext> multiOrientedReaderContexts() {
        if(multiOrientedReaderContexts == null) {
            multiOrientedReaderContexts =  new HashMap<>();
        }
        return multiOrientedReaderContexts;
    }
}
