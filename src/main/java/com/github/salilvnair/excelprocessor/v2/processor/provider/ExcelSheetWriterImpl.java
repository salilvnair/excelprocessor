package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriteException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

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
        }
    }

    @Override
    public <T extends BaseSheet> Workbook workbook(List<T> sheetData, ExcelSheetContext sheetContext) {
        if(CollectionUtils.isEmpty(sheetData)) {
            return null;
        }
        BaseExcelSheetWriter writer = ExcelSheetFactory.generateWriter(sheetData.get(0).getClass());
        ExcelSheetWriterContext context = buildExcelSheetWriterContext(sheetContext);
        if(writer != null) {
            writer.write(sheetData, context);
        }
        return context.workbook();
    }

    private ExcelSheetWriterContext buildExcelSheetWriterContext(ExcelSheetContext context) {
        File excelFile = context.excelFile();
        Workbook workbook = context.workbook();
        if(excelFile !=null && workbook == null) {
            FileInputStream inputS = null;
            try {
                inputS = new FileInputStream(excelFile);
                workbook = ExcelSheetReaderUtil.generateWorkbook(inputS, excelFile.getAbsolutePath());
                context.setFileName(excelFile.getName());
            }
            catch (Exception e) {
                if(!context.suppressExceptions()) {
                    throw new ExcelSheetWriteException(e);
                }
            }
        }
        return ExcelSheetWriterContext
                .builder()
                .template(workbook)
                .build();
    }
}
