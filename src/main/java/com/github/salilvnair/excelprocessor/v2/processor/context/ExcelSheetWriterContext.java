package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.helper.style.CellStyleRegistry;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import lombok.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Salil V Nair
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSheetWriterContext extends BaseExcelSheetContext {

    private Workbook template;

    private Workbook styleTemplate;

    private Workbook existingWorkbook;

    private boolean streamingWorkbook;

    private boolean containsExistingWorkbook;

    private Cell rowCell;

    private Field cellField;

    private Object cellValue;

    private String header;

    private List<? extends BaseSheet> sheetData;

    private BaseSheet sheetDataObj;

    private Set<String> orderedHeaders;

    private Map<String, String> dynamicHeaderDisplayNames;

    private Map<String, DataCellStyleInfo> dynamicHeaderDataCellStyleInfo;

    private Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo;

    private List<Object> taskMetadata;

    private boolean suppressExceptions;

    private boolean suppressTaskExceptions;

    private AbstractExcelTask taskBean;

    private Function<String, Object> beanResolver;

    private CellStyle defaultHeaderCellStyle;

    private CellStyle defaultDataCellStyle;

    private Map<String, CellStyle> headerCellStyleMap;

    private Map<String, CellStyle> dataCellStyleMap;

    /** workbook-scoped style/font cache; never create styles per cell, resolve them here */
    private CellStyleRegistry styleRegistry;

    /** formula evaluator reused for every cell of {@link #formulaEvaluatorWorkbook} */
    private FormulaEvaluator formulaEvaluator;

    private Workbook formulaEvaluatorWorkbook;

    public CellStyleRegistry styleRegistry() {
        if(styleRegistry == null) {
            styleRegistry = new CellStyleRegistry();
        }
        return styleRegistry;
    }

    /** Evaluates {@code cell} only when it holds a formula, reusing one evaluator per workbook. */
    public void evaluateIfFormula(Cell cell) {
        if(cell == null || cell.getCellType() != org.apache.poi.ss.usermodel.CellType.FORMULA) {
            return;
        }
        Workbook cellWorkbook = cell.getSheet().getWorkbook();
        if(formulaEvaluator == null || formulaEvaluatorWorkbook != cellWorkbook) {
            formulaEvaluator = cellWorkbook.getCreationHelper().createFormulaEvaluator();
            formulaEvaluatorWorkbook = cellWorkbook;
        }
        formulaEvaluator.evaluateFormulaCell(cell);
    }

    public Workbook workbook() {
        return super.getWorkbook();
    }

    public boolean streamingWorkbook() {
        return streamingWorkbook;
    }
    public boolean containsExistingWorkbook() {
        return containsExistingWorkbook;
    }

    public Workbook template() {
        return this.template;
    }

    public Workbook styleTemplate() {
        return this.styleTemplate;
    }
    public Workbook existingWorkbook() {
        return this.existingWorkbook;
    }


    public Cell rowCell() {
        return  rowCell;
    }

    public Field cellField() {
        return  cellField;
    }

    public Object cellValue() {
        return cellValue;
    }

    public List<? extends BaseSheet> sheetData() {
        return  sheetData;
    }

    public BaseSheet sheetDataObj() {
        return  sheetDataObj;
    }
    public List<Object> taskMetadata() {
        return  taskMetadata;
    }

    public boolean suppressExceptions() {
        return suppressExceptions;
    }

    public boolean suppressTaskExceptions() {
        return suppressTaskExceptions;
    }

    public <T> T sheet(Class<T> clazz) {
        if(clazz.isInstance(sheetDataObj)) {
            return clazz.cast(sheetDataObj);
        }
        return null;
    }

    public CellStyle defaultDataCellStyle() {
        return this.defaultDataCellStyle;
    }

    public CellStyle dataCellStyle(String key) {
        if(this.dataCellStyleMap != null && dataCellStyleMap.containsKey(key)) {
            return dataCellStyleMap.get(key);
        }
        return null;
    }

    public boolean hasDataCellStyle(String key) {
        return this.dataCellStyleMap!=null && dataCellStyleMap.containsKey(key);
    }

    public void addDataCellStyle(String key, CellStyle cellStyle) {
        if(dataCellStyleMap == null) {
            dataCellStyleMap = new HashMap<>();
        }
        dataCellStyleMap.put(key, cellStyle);
    }

    public Function<String, Object> beanResolver() {
        return this.beanResolver;
    }

}
