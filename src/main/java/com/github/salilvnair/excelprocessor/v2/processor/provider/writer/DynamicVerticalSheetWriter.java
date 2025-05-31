package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.BaseExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import java.util.*;


public class DynamicVerticalSheetWriter extends BaseExcelSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext writerContext) {
        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Sheet sheet = dynamicHeaderSheet.getClass().getAnnotation(Sheet.class);
        writerContext.setSheetData(sheetData);
        writerContext.setSheetDataObj(dynamicHeaderSheet);
        Map<String, CellInfo> headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);

        Workbook workbook = writerContext.containsExistingWorkbook() ? writerContext.existingWorkbook() : writerContext.template() == null ? ExcelSheetWriterUtil.generateWorkbook(sheet) : writerContext.template();
        org.apache.poi.ss.usermodel.Sheet workbookSheet =  writerContext.template() == null ? workbook.createSheet(sheet.value()): workbook.getSheet(sheet.value());
        if(workbookSheet == null) {
            workbookSheet = workbook.createSheet(sheet.value());
        }
        writeDynamicDataToHeader(headerKeyedCellInfoMap, workbookSheet, sheet, writerContext);
        writeDynamicDataToBody(headerKeyedCellInfoMap, sheetData, workbookSheet, sheet, writerContext);
        applySheetStyles(sheet, workbook, workbookSheet, writerContext);
        writerContext.setWorkbook(workbook);
    }

    protected void writeDynamicDataToHeader(Map<String, CellInfo> headerKeyedCellInfoMap, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        int headerRowEndsIndex = sheet.headerRowEndsAt();
        headerRowEndsIndex = headerRowEndsIndex != -1 ? headerRowEndsIndex : (headerRowIndex + headerKeyedCellInfoMap.size() - 1);

        Set<String> headers = headerKeyedCellInfoMap.keySet();
        List<String> headerList = new ArrayList<>(headers);
        for (int r = headerRowIndex; r <= headerRowEndsIndex; r++) {
            Row row = workbookSheet.createRow(r);
            org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(headerColumnIndex);
            String headerKey = headerList.get(r - headerRowIndex);
            convertAndSetCellValue(rowCell, headerKey);
            applyDynamicHeaderCellStyles(sheet, headerKey, rowCell, context);
        }
    }

    protected void writeDynamicDataToBody(Map<String, CellInfo> headerKeyedCellInfoMap, List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        int headerRowEndsIndex = sheet.headerRowEndsAt();
        headerRowEndsIndex = headerRowEndsIndex != -1 ? headerRowEndsIndex : (headerRowIndex + headerKeyedCellInfoMap.size() - 1);
        String valueColumnAt = sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        String valueColumnBeginsAt = sheet.valueColumnBeginsAt();
        int valueColumnBeginsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnBeginsAt)  - 1;
        valueColumnBeginsAtIndex = valueColumnBeginsAtIndex!= -1 ? valueColumnBeginsAtIndex : valueColumnIndex;

        Map<Integer, CellInfo> headerRowIndexCellInfoMap = new HashMap<>();
        for (int r = headerRowIndex; r <= headerRowEndsIndex; r++) {
            org.apache.poi.ss.usermodel.Cell cell = workbookSheet.getRow(r).getCell(headerColumnIndex);
            CellInfo cellInfo = new CellInfo();
            Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbookSheet.getWorkbook(), cell, cellInfo);
            cellInfo.setRowIndex(r);
            cellInfo.setColumnIndex(headerColumnIndex);
            String originalHeader = ExcelSheetReaderUtil.cleanHeaderString(value+"");
            String header = originalHeader + "_" + r;
            cellInfo.setHeader(header);
            cellInfo.setOriginalHeader(originalHeader);
            headerRowIndexCellInfoMap.put(r, cellInfo);
        }

        int columnIndex = valueColumnBeginsAtIndex;
        for (BaseSheet sheetDataObj : sheetData) {
            context.setSheetDataObj(sheetDataObj);
            DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetDataObj;
            headerKeyedCellInfoMap  = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
            if(headerKeyedCellInfoMap == null || headerKeyedCellInfoMap.isEmpty()) {
                continue;
            }
            for (int r = headerRowIndex; r <= headerRowEndsIndex; r++) {
                Row row = workbookSheet.getRow(r) == null ? workbookSheet.createRow(r) : workbookSheet.getRow(r);
                CellInfo dynamicallyGeneratedCellInfo = headerRowIndexCellInfoMap.get(r);
                String headerKey = dynamicallyGeneratedCellInfo.getOriginalHeader();
                CellInfo userProvidedCellInfo = headerKeyedCellInfoMap.get(headerKey);
                if (userProvidedCellInfo == null) {
                    headerKey = dynamicallyGeneratedCellInfo.getHeader();
                    userProvidedCellInfo = headerKeyedCellInfoMap.get(headerKey);
                }
                org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(columnIndex) == null ? row.createCell(columnIndex) : row.getCell(columnIndex);
                convertAndSetCellValue(rowCell, userProvidedCellInfo.value());
                applyDynamicHeaderCellStyles(sheet, headerKey, rowCell, context);
            }
            columnIndex++;
        }
    }
}
