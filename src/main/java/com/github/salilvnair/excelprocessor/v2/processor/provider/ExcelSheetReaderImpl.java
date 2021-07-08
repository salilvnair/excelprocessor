package com.github.salilvnair.excelprocessor.v2.processor.provider;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.task.ExcelSheetReaderTask;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;

public class ExcelSheetReaderImpl extends BaseExcelProcessor implements ExcelSheetReader {
    private boolean concurrent = false;
    protected ExcelSheetReaderImpl() {}
    protected ExcelSheetReaderImpl(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public static ExcelSheetReaderImpl init() {
        return new ExcelSheetReaderImpl(false);
    }
    public static ExcelSheetReaderImpl init(boolean concurrent) {
        return new ExcelSheetReaderImpl(concurrent);
    }

    @Override
    public Map<String, List<? extends BaseExcelSheet>> read(String[] fullyQualifiedClassNames, ExcelSheetContext context) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        _readAndResolve(fullyQualifiedClassNames, context, excelSheets);
        return excelSheets;
    }


    @Override
    public Map<String, List<? extends BaseExcelSheet>> read(Class<? extends BaseExcelSheet>[] classes, ExcelSheetContext context) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        for (Class<? extends BaseExcelSheet> clazz : classes) {
            ExcelSheetReaderContext readerContext = _read(context, clazz);
            _resolve(readerContext, context, excelSheets);
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
    public List<CellValidationMessage> validate(List<? extends BaseExcelSheet> sheetData, ExcelSheetContext sheetContext) throws Exception {
        if(sheetData!=null && !sheetData.isEmpty()) {
            return _validate(sheetData.get(0).getClass(), sheetContext.readerContext(), sheetContext);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<CellValidationMessage>> validate(Map<String, List<? extends BaseExcelSheet>> excelData, ExcelSheetContext sheetContext) throws Exception {
        return _validate(null, excelData, sheetContext);
    }

    @Override
    public void readAndValidate(Class<? extends BaseExcelSheet> clazz, ExcelSheetContext sheetContext) throws Exception {
        List<CellValidationMessage> validationMessages = _readAndValidate(clazz, sheetContext);
        sheetContext.setSheetValidationMessages(validationMessages);
    }

    @Override
    public void readAndValidate(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        Map<String, List<CellValidationMessage>> excelValidationMessages = new HashMap<>();
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
        Map<String, List<CellValidationMessage>> excelValidationMessages = new HashMap<>();
        for (Class<? extends BaseExcelSheet> clazz: classes) {
            _readAndValidate(sheetContext, excelSheets, excelValidationMessages, clazz);
        }
        sheetContext.setExcelSheets(excelSheets);
        sheetContext.setExcelValidationMessages(excelValidationMessages);
    }

    @Override
    public <T extends BaseExcelSheet> ExcelInfo excelInfo(Class<T> clazz, ExcelSheetContext sheetContext) throws Exception {
        return _excelInfo(clazz, sheetContext);
    }

    @Override
    public ExcelInfo excelInfo(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) throws Exception {
        ExcelInfo excelInfo = new ExcelInfo();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseExcelSheet> clazz = Class.forName(clazzName).asSubclass(BaseExcelSheet.class);
            ExcelInfo excelInfoItr = _excelInfo(clazz, sheetContext);
            excelInfo.sheets().addAll(excelInfoItr.sheets());
        }
        return excelInfo;
    }


    public <T extends BaseExcelSheet> ExcelInfo _excelInfo(Class<T> clazz, ExcelSheetContext context) throws Exception {
        ExcelSheetReaderContext readerContext =  new ExcelSheetReaderContext();
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        BaseExcelSheetReader sheetReader = _sheetReader(readerContext, clazz, context, multiOrientedSheet!=null);
        if (sheetReader != null) {
            return sheetReader.excelInfo(clazz, readerContext);
        }
        return null;
    }
    private void _resolve(ExcelSheetReaderContext readerContext, ExcelSheetContext context, Map<String, List<? extends BaseExcelSheet>> excelSheets) {
        String sheetName = readerContext.sheetName();
        boolean multiOriented = readerContext.extractMultiOrientedMap();
        context.readerContexts().put(sheetName, readerContext);
        if(multiOriented) {
            Map<String, List<? extends BaseExcelSheet>> multiOrientedSheetMap = readerContext.getMultiOrientedSheetMap();
            List<? extends BaseExcelSheet> multiOrientedSheetData = multiOrientedSheetMap.entrySet().stream().flatMap(e-> e.getValue().stream()).collect(Collectors.toList());
            excelSheets.put(sheetName, multiOrientedSheetData);
        }
        else {
            excelSheets.put(sheetName, readerContext.getSheetData());
        }
    }

    private void _readAndResolve(String[] fullyQualifiedClassNames, ExcelSheetContext context, Map<String, List<? extends BaseExcelSheet>> excelSheets) throws Exception {
        if(concurrent) {
            _concurrentRead(fullyQualifiedClassNames, context, excelSheets);
        }
        else {
            for (String clazzName : fullyQualifiedClassNames) {
                Class<? extends BaseExcelSheet> clazz = Class.forName(clazzName).asSubclass(BaseExcelSheet.class);
                ExcelSheetReaderContext readerContext = _read(context, clazz);
                _resolve(readerContext, context, excelSheets);
            }
        }
    }

    private void _concurrentRead(String[] fullyQualifiedClassNames, ExcelSheetContext context, Map<String, List<? extends BaseExcelSheet>> excelSheets) throws Exception {
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        List<Future<ExcelSheetReaderContext>> futureList = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseExcelSheet> clazz = Class.forName(clazzName).asSubclass(BaseExcelSheet.class);
            ExcelSheetReaderTask task = new ExcelSheetReaderTask(TaskType.READ_MULTIPLE_SHEETS.name(), null, service, context, clazz);
            taskCallables.add(task);
        }
        try {
            futureList = executor.invokeAll(taskCallables);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(futureList!=null) {
            for(Future<ExcelSheetReaderContext> futureContext : futureList) {
                ExcelSheetReaderContext readerContext = futureContext.get();
                _resolve(readerContext, context, excelSheets);
            }
        }
        executor.shutdown();
    }

    protected ExcelSheetReaderContext _read(ExcelSheetContext context, Class<? extends BaseExcelSheet> clazz) throws Exception {
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        String sheetName = null;
        if(sheet !=null) {
            sheetName = sheet.value();
        }
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        boolean multiOriented = false;
        if(multiOrientedSheet!=null) {
            multiOriented = true;
            sheetName = multiOrientedSheet.name();
        }
        ExcelSheetReaderContext readerContext = _read(clazz, context, multiOriented);
        readerContext.setSheetName(sheetName);
        readerContext.setExtractMultiOrientedMap(multiOriented);
        return readerContext;
    }

    private  <T extends BaseExcelSheet> ExcelSheetReaderContext _read(Class<T> clazz, ExcelSheetContext context, boolean multiOriented) throws Exception {
        ExcelSheetReaderContext readerContext =  new ExcelSheetReaderContext();
        BaseExcelSheetReader sheetReader = _sheetReader(readerContext, clazz, context, multiOriented);
        if (sheetReader != null) {
            sheetReader.read(clazz, readerContext);
        }
        return readerContext;
    }

    private  <T extends BaseExcelSheet> BaseExcelSheetReader _sheetReader(ExcelSheetReaderContext readerContext, Class<T> clazz, ExcelSheetContext context, boolean multiOriented) throws Exception {
        File excelFile = context.getExcelFile();
        readerContext.setWorkbook(context.getWorkbook());
        if(excelFile !=null && context.getWorkbook() == null) {
            FileInputStream inputS = new FileInputStream(excelFile);
            readerContext.setWorkbook(ExcelSheetReader.generateWorkbook(inputS, excelFile.getAbsolutePath()));
        }
        readerContext.setFileName(context.getFileName());
        readerContext.setExtractMultiOrientedMap(multiOriented);
        return ExcelSheetFactory.generateReader(clazz, concurrent);
    }

    private List<CellValidationMessage> _readAndValidate(Class<? extends BaseExcelSheet> clazz, ExcelSheetContext sheetContext) throws Exception {
        ExcelSheetReaderContext readerContext = _read(clazz, sheetContext, false);
        sheetContext.setSheet(readerContext.getSheetData());
        return _validate(clazz, readerContext, sheetContext);
    }

    private void _readAndValidate(ExcelSheetContext sheetContext, Map<String, List<? extends BaseExcelSheet>> excelSheets, Map<String, List<CellValidationMessage>> excelValidationMessages, Class<? extends BaseExcelSheet> clazz) throws Exception {
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        boolean multiOriented = multiOrientedSheet != null;
        ExcelSheetReaderContext readerContext = _read(clazz, sheetContext, multiOriented);
        if(multiOriented) {
            Map<String, List<? extends BaseExcelSheet>> multiOrientedSheetMap = readerContext.getMultiOrientedSheetMap();
            List<? extends BaseExcelSheet> multiOrientedSheetData = multiOrientedSheetMap.entrySet().stream().flatMap(e-> e.getValue().stream()).collect(Collectors.toList());
            excelSheets.put(multiOrientedSheet.name(), multiOrientedSheetData);
            sheetContext.setReaderContexts(readerContext.multiOrientedReaderContexts());
            Map<String, List<CellValidationMessage>> validationMessageMap = _validate(multiOrientedSheet, multiOrientedSheetMap, sheetContext);
            List<CellValidationMessage> validationMessages = validationMessageMap.entrySet().stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
            excelValidationMessages.put(multiOrientedSheet.name(), validationMessages);
        }
        else {
            List<? extends BaseExcelSheet> sheetData = readerContext.getSheetData();
            excelSheets.put(sheet.value(), sheetData);
            List<CellValidationMessage> validationMessages = _validate(clazz, readerContext, sheetContext);
            excelValidationMessages.put(sheet.value(), validationMessages);
        }
    }

    private Map<String, List<CellValidationMessage>> _validate(MultiOrientedSheet multiOrientedSheet, Map<String, List<? extends BaseExcelSheet>> excelData, ExcelSheetContext sheetContext) throws Exception {
        Map<String, List<CellValidationMessage>> excelValidationMessages = new HashMap<>();
        excelData.forEach((headerKey, excelSheet) -> {
            ExcelSheetReaderContext readerContext = sheetContext.readerContexts().get(headerKey);
            readerContext.setSheetData(excelSheet);
            List<CellValidationMessage> validationMessages = _validate(multiOrientedSheet, excelSheet.get(0).getClass(), readerContext, sheetContext);
            excelValidationMessages.put(headerKey, validationMessages);
        });
        return excelValidationMessages;
    }

    private  <T extends BaseExcelSheet> List<CellValidationMessage> _validate(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext readerContext, ExcelSheetContext sheetContext) {
        return _validate(null, clazz, readerContext, sheetContext);
    }

    private  <T extends BaseExcelSheet> List<CellValidationMessage> _validate(MultiOrientedSheet multiOrientedSheet, Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext readerContext, ExcelSheetContext sheetContext) {
        CellValidatorContext validatorContext = new CellValidatorContext();
        validatorContext.setReaderContext(readerContext);
        if(multiOrientedSheet!=null) {
            validatorContext.setSheetName(multiOrientedSheet.name());
        }
        List<? extends BaseExcelSheet> rows = readerContext.getSheetData();
        return ExcelValidator
                .init(validatorContext)
                .setUserValidatorMap(sheetContext.userValidatorMap())
                .setValidValuesDataSet(sheetContext.validValuesDataSet())
                .setUserDefinedMessageDataSet(sheetContext.userDefinedMessageDataSet())
                .rows(rows)
                .validate();
    }
}
