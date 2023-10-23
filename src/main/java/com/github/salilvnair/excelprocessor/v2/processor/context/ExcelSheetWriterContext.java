package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import lombok.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private CellStyle defaultHeaderCellStyle;

    private CellStyle defaultDataCellStyle;

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

}
