package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class UserDefinedTemplateHorizontalSheetWriter extends BaseHorizontalSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext writerContext) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        Workbook workbook = writerContext.containsExistingWorkbook() ? writerContext.existingWorkbook() : writerContext.template();
        org.apache.poi.ss.usermodel.Sheet workbookSheet =  workbook.getSheet(sheet.value());
        List<Field> cells = new ArrayList<>(cellFields);
        writeUserDefinedTemplateDataToBody(sheetData, cellFields, workbookSheet, cells, sheet, writerContext);
        applySheetStyles(sheet, workbook, workbookSheet, writerContext);
        writerContext.setWorkbook(workbook);
    }
}
