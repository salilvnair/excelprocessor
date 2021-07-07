package com.github.salilvnair.excelprocessor.v2.processor.provider;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

public class ExcelSheetReaderImpl extends BaseExcelProcessor implements ExcelSheetReader {
    private ExcelSheetReaderImpl() {}

    public static ExcelSheetReaderImpl init() {
        return new ExcelSheetReaderImpl();
    }

    @Override
    public Map<String, List<? extends BaseExcelSheet>> read(String[] fullyQualifiedClassNames, ExcelSheetContext context) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseExcelSheet> clazz = Class.forName(clazzName).asSubclass(BaseExcelSheet.class);
            _read(context, excelSheets, clazz);
        }
        return excelSheets;
    }

    @Override
    public Map<String, List<? extends BaseExcelSheet>> read(Class<? extends BaseExcelSheet>[] classes, ExcelSheetContext context) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        for (Class<? extends BaseExcelSheet> clazz : classes) {
            _read(context, excelSheets, clazz);
        }
        return excelSheets;
    }

    @Override
    public final <T extends BaseExcelSheet> List<T> read(Class<T> clazz, ExcelSheetContext context) throws Exception {
        ExcelSheetReaderContext readerContext = _read(clazz, context, false);
        return typedList(readerContext.getSheetData(), clazz);
    }

    @Override
    public <T extends BaseExcelSheet> Map<String, List<? extends BaseExcelSheet>> read(Class<T> clazz, boolean multiOriented, ExcelSheetContext context) throws Exception {
        ExcelSheetReaderContext readerContext = _read(clazz, context, multiOriented);
        return readerContext.getMultiOrientedSheetMap();
    }

    @Override
    public List<ValidationMessage> validate(List<? extends BaseExcelSheet> sheetData, ExcelSheetContext sheetContext) throws Exception {
        if(sheetData!=null && !sheetData.isEmpty()) {
            return _validate(sheetData.get(0).getClass(), sheetContext.readerContext());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<ValidationMessage>> validate(Map<String, List<? extends BaseExcelSheet>> excelData, ExcelSheetContext sheetContext) throws Exception {
        Map<String, List<ValidationMessage>> excelValidationMessages = new HashMap<>();
        excelData.forEach((headerKey, excelSheet) -> {
            ExcelSheetReaderContext readerContext = sheetContext.readerContexts().get(headerKey);
            readerContext.setSheetData(excelSheet);
            List<ValidationMessage> validationMessages = _validate(excelSheet.get(0).getClass(), readerContext);
            excelValidationMessages.put(headerKey, validationMessages);
        });
        return excelValidationMessages;
    }

    @Override
    public void readAndValidate(Class<? extends BaseExcelSheet> clazz, ExcelSheetContext sheetContext) throws Exception {
        List<ValidationMessage> validationMessages = _readAndValidate(clazz, sheetContext);
        sheetContext.setSheetValidationMessages(validationMessages);
    }

    @Override
    public void readAndValidate(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        Map<String, List<ValidationMessage>> excelValidationMessages = new HashMap<>();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseExcelSheet> clazz = Class.forName(clazzName).asSubclass(BaseExcelSheet.class);
            _readAndValidate(sheetContext, excelSheets, excelValidationMessages, clazz);
        }
        sheetContext.setExcelSheets(excelSheets);
        sheetContext.setExcelValidationMessages(excelValidationMessages);
    }

    @Override
    public void readAndValidate(Class<? extends BaseExcelSheet>[] classes, ExcelSheetContext sheetContext) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        Map<String, List<ValidationMessage>> excelValidationMessages = new HashMap<>();
        for (Class<? extends BaseExcelSheet> clazz: classes) {
            _readAndValidate(sheetContext, excelSheets, excelValidationMessages, clazz);
        }
        sheetContext.setExcelSheets(excelSheets);
        sheetContext.setExcelValidationMessages(excelValidationMessages);
    }

    private void _readAndValidate(ExcelSheetContext sheetContext, Map<String, List<? extends BaseExcelSheet>> excelSheets, Map<String, List<ValidationMessage>> excelValidationMessages, Class<? extends BaseExcelSheet> clazz) throws Exception {
        ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
        ExcelSheetReaderContext readerContext = _read(clazz, sheetContext, false);
        List<? extends BaseExcelSheet> sheetData = readerContext.getSheetData();
        List<ValidationMessage> validationMessages = _validate(clazz, readerContext);
        excelValidationMessages.put(excelSheet.value(), validationMessages);
        excelSheets.put(excelSheet.value(), sheetData);
    }

    private void _read(ExcelSheetContext context, Map<String, List<? extends BaseExcelSheet>> excelSheets, Class<? extends BaseExcelSheet> clazz) throws Exception {
        ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
        String sheetName = null;
        if(excelSheet!=null) {
            sheetName = excelSheet.value();
        }
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        boolean multiOriented = false;
        if(multiOrientedSheet!=null) {
            multiOriented = true;
            sheetName = multiOrientedSheet.name();
        }
        ExcelSheetReaderContext readerContext = _read(clazz, context, multiOriented);
        context.readerContexts().put(sheetName, readerContext);
        excelSheets.put(sheetName, readerContext.getSheetData());
    }

    private  <T extends BaseExcelSheet> ExcelSheetReaderContext _read(Class<T> clazz, ExcelSheetContext context, boolean multiOriented) throws Exception {
        ExcelSheetReaderContext readerContext =  new ExcelSheetReaderContext();
        File excelFile = context.getExcelFile();
        readerContext.setWorkbook(context.getWorkbook());
        if(excelFile !=null) {
            FileInputStream inputS = new FileInputStream(excelFile);
            readerContext.setWorkbook(ExcelSheetReader.generateWorkbook(inputS, excelFile.getAbsolutePath()));
        }
        readerContext.setFileName(context.getFileName());
        readerContext.setExtractMultiOrientedMap(multiOriented);
        BaseExcelSheetReader sheetReader = ExcelSheetFactory.generateReader(clazz);
        if (sheetReader != null) {
            sheetReader.read(clazz, readerContext);
        }
        return readerContext;
    }

    private List<ValidationMessage> _readAndValidate(Class<? extends BaseExcelSheet> clazz, ExcelSheetContext sheetContext) throws Exception {
        ExcelSheetReaderContext readerContext = _read(clazz, sheetContext, false);
        sheetContext.setSheet(readerContext.getSheetData());
        return _validate(clazz, readerContext);
    }

    private  <T extends BaseExcelSheet> List<ValidationMessage> _validate(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext readerContext) {
        ValidatorContext validatorContext = new ValidatorContext();
        validatorContext.setReaderContext(readerContext);
        List<? extends BaseExcelSheet> rows = readerContext.getSheetData();
        return ExcelValidator.init(validatorContext).rows(rows).validate();
    }
}
