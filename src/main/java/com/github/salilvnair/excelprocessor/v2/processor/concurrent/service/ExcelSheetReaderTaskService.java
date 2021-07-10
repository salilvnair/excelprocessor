package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.concurrent.ConcurrentTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderTaskService implements ConcurrentTaskService<ExcelSheetReaderContext> {
    private ExcelSheetReaderConcurrentService excelSheetReaderConcurrentService = getExcelSheetReaderConcurrentService();
    private SheetReaderConcurrentService sheetReaderConcurrentService;
    @Override
    public ExcelSheetReaderContext toContext(String taskType, Map<String, Object> taskParams, Object... args) {
        if(TaskType.READ_MULTIPLE_SHEETS.name().equals(taskType)) {
            return readMultipleSheets(taskType, taskParams, args);
        }
        else if(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name().equals(taskType)) {
            return readMultipleRowsOrColumns(taskType, taskParams, args);
        }

        return null;
    }

    private ExcelSheetReaderContext readMultipleSheets(String taskType, Map<String, Object> taskParams, Object... args) {
        ExcelSheetContext context = (ExcelSheetContext) args[0];
        Object oClazz = args[1];
        try {
            String class_ = oClazz.toString().replace("class ","");
            Class<? extends BaseSheet> clazz = Class.forName(class_).asSubclass(BaseSheet.class);
            return excelSheetReaderConcurrentService.read(context, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private ExcelSheetReaderContext readMultipleRowsOrColumns(String taskType, Map<String, Object> taskParams, Object... args) {
        Object oClazz = args[0];
        try {
            String class_ = oClazz.toString().replace("class ","");
            Class<? extends BaseSheet> clazz = Class.forName(class_).asSubclass(BaseSheet.class);
            ExcelSheetReaderContext context = (ExcelSheetReaderContext) args[1];
            Workbook workbook = (Workbook) args[2];
            List<BaseSheet> baseSheetList = (List<BaseSheet>) args[3];
            Map<Integer, String> headerColumnIndexKeyedHeaderValueMap = (Map<Integer, String>) args[4];
            Map<Integer, Map<String, CellInfo >> rowIndexKeyedHeaderKeyCellInfoMap = (Map<Integer, Map<String, CellInfo>>) args[5];
            sheetReaderConcurrentService(clazz).read(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, args[6]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ExcelSheetReaderConcurrentService getExcelSheetReaderConcurrentService() {
        return new ExcelSheetReaderConcurrentServiceImpl(true, 100);
    }

    public void setExcelSheetReaderConcurrentService(ExcelSheetReaderConcurrentService excelSheetReaderConcurrentService) {
        this.excelSheetReaderConcurrentService = excelSheetReaderConcurrentService;
    }

    public SheetReaderConcurrentService sheetReaderConcurrentService(Class<? extends BaseSheet> clazz) {
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(sheet.isVertical()) {
            if(sheet.dynamicHeaders()) {
                return new DynamicHeaderVerticalSheetReaderConcurrentServiceImpl(true, 100);
            }
            return new VerticalSheetReaderConcurrentServiceImpl(true, 100);
        }
        else {
            if(sheet.dynamicHeaders()) {
                return new DynamicHeaderHorizontalSheetReaderConcurrentServiceImpl(true, 100);
            }
            return new HorizontalSheetReaderConcurrentServiceImpl(true, 100);
        }
    }

    public void setSheetReaderConcurrentService(SheetReaderConcurrentService sheetReaderConcurrentService) {
        this.sheetReaderConcurrentService = sheetReaderConcurrentService;
    }
}
