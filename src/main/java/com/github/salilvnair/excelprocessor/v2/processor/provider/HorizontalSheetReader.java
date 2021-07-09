package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.ConcurrentUtil;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Salil V Nair
 */
public class HorizontalSheetReader extends BaseHorizontalSheetReader {
    public HorizontalSheetReader(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }

    protected Map<Cell, Field> headerCellFieldMap(ExcelSheetReaderContext context, Sheet sheet, Set<Field> excelHeaders) {
        return excelHeaders.stream().collect(Collectors.toMap(excelHeader -> excelHeader.getAnnotation(Cell.class), excelHeader -> excelHeader, (o, n) -> n));
    }

    protected BaseSheet cellValueResolver(Class<? extends BaseSheet> clazz, Map<String, CellInfo> excelCellInfoMap, int key, Map<Cell, Field> headerCellFieldMap) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        classObject.setRowIndex(key);
        classObject.setRow(key+1);
        headerCellFieldMap.forEach((cell, field) -> {
            String headerString = cell.value();
            headerString = cleanAndProcessSimilarHeaderString(headerString, clazz, cell);
            CellInfo cellInfo = excelCellInfoMap.get(headerString);
            if(cellInfo!=null) {
                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType());
                ReflectionUtil.setField(classObject, field, fieldValue);
            }
        });
        return classObject;
    }

}
