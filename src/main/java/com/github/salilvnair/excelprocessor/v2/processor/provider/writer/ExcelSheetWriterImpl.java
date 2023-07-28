package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExcelSheetWriterImpl implements ExcelSheetWriter {
    private ExcelSheetWriterImpl() {}
    public static ExcelSheetWriterImpl init() {
        return new ExcelSheetWriterImpl();
    }
    @Override
    public <T extends BaseSheet> void write(List<T> sheetData, ExcelSheetContext sheetContext) throws Exception {
        Workbook workbook = workbook(sheetData, sheetContext);
        if(workbook != null) {
            ExcelSheetWriterUtil.write(workbook, sheetContext.fileName(), sheetContext.filePath());
            disposeStreamingWorkbook(workbook, sheetContext);
        }
    }

    @Override
    public <T extends BaseSheet> Workbook workbook(List<T> sheetData, ExcelSheetContext sheetContext) {
        return extractWorkbookBySheetData(sheetData, sheetContext, false);
    }

    @Override
    public Workbook workbook(Map<String, List<? extends BaseSheet>> sheets, ExcelSheetContext sheetContext) {
        if(sheets == null || sheets.isEmpty()) {
            return null;
        }
        Set<String> sheetNames = sheets.keySet();
        boolean containsExistingWorkbook = false;
        for (String sheetName: sheetNames) {
            Workbook existingWorkbook = extractWorkbookBySheetData(sheets.get(sheetName), sheetContext, containsExistingWorkbook);
            sheetContext.setWorkbook(existingWorkbook);
            containsExistingWorkbook = true;
        }
        return sheetContext.writerContext().workbook();
    }

    private <T extends BaseSheet> Workbook extractWorkbookBySheetData(List<T> sheetData, ExcelSheetContext sheetContext, boolean containsExistingWorkbook) {
        if(CollectionUtils.isEmpty(sheetData)) {
            return null;
        }
        BaseExcelSheetWriter writer = ExcelSheetFactory.generateWriter(sheetData.get(0).getClass());
        ExcelSheetWriterContext writerContext = buildExcelSheetWriterContext(sheetContext, containsExistingWorkbook);
        sheetContext.setWriterContext(writerContext);
        writerContext.setContainsExistingWorkbook(containsExistingWorkbook);
        if(writer != null) {
            writer.write(sheetData, writerContext);
        }
        return writerContext.workbook();
    }

    private ExcelSheetWriterContext buildExcelSheetWriterContext(ExcelSheetContext sheetContext, boolean existingWorkbook) {
        File excelFile = sheetContext.excelFile();
        Workbook workbook = sheetContext.workbook();
        existingWorkbook = existingWorkbook || sheetContext.template() != null;
        if(excelFile !=null && workbook == null && !existingWorkbook) {
            FileInputStream inputS = null;
            try {
                inputS = new FileInputStream(excelFile);
                workbook = ExcelSheetWriterUtil.generateWorkbook(inputS, excelFile.getAbsolutePath());
                sheetContext.setFileName(excelFile.getName());
            }
            catch (Exception e) {
                if(!sheetContext.suppressExceptions()) {
                    throw new ExcelSheetWriterException(e);
                }
            }
        }
        return ExcelSheetWriterContext
                .builder()
                .containsExistingWorkbook(existingWorkbook)
                .existingWorkbook(existingWorkbook ? sheetContext.workbook() : null)
                .template(workbook)
                .build();
    }
}
