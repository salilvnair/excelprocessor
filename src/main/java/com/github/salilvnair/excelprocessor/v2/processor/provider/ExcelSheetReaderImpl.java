package com.github.salilvnair.excelprocessor.v2.processor.provider;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReadException;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.task.ExcelSheetReaderTask;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelSheetValidator;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;

public class ExcelSheetReaderImpl extends BaseExcelProcessor implements ExcelSheetReader {
    private boolean concurrent = false;
    private int batchSize = 0;
    protected ExcelSheetReaderImpl() {}
    protected ExcelSheetReaderImpl(boolean concurrent, int batchSize) {
        this.concurrent = concurrent;
        this.batchSize = batchSize;
    }

    public static ExcelSheetReaderImpl init() {
        return new ExcelSheetReaderImpl();
    }
    public static ExcelSheetReaderImpl init(boolean concurrent, int batchSize) {
        return new ExcelSheetReaderImpl(concurrent, batchSize);
    }

    @Override
    public Map<String, List<? extends BaseSheet>> read(String[] fullyQualifiedClassNames, ExcelSheetContext context) {
        Map<String, List<? extends BaseSheet>> excelSheets = new HashMap<>();
        _readAndResolve(fullyQualifiedClassNames, context, excelSheets);
        return excelSheets;
    }


    @Override
    public Map<String, List<? extends BaseSheet>> read(Class<? extends BaseSheet>[] classes, ExcelSheetContext context) {
        Map<String, List<? extends BaseSheet>> excelSheets = new HashMap<>();
        for (Class<? extends BaseSheet> clazz : classes) {
            ExcelSheetReaderContext readerContext = _read(context, clazz);
            _resolve(readerContext, context, excelSheets);
        }
        return excelSheets;
    }

    @Override
    public final <T extends BaseSheet> List<T> read(Class<T> clazz, ExcelSheetContext context) {
        ExcelSheetReaderContext readerContext = _read(clazz, context, false);
        List<T> sheetData = typedList(readerContext.getSheetData(), clazz);
        context.setSheet(sheetData);
        context.setReaderContext(readerContext);
        return sheetData;
    }

    @Override
    public <T extends BaseSheet> Map<String, List<? extends BaseSheet>> read(Class<T> clazz, boolean multiOriented, ExcelSheetContext context) {
        ExcelSheetReaderContext readerContext = _read(clazz, context, multiOriented);
        Map<String, List<? extends BaseSheet>> excelSheetMap = new HashMap<>();
        if(!multiOriented) {
            List<? extends BaseSheet> sheetData = readerContext.getSheetData();
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            excelSheetMap.put(sheet.value(), sheetData);
        }
        else {
            excelSheetMap = readerContext.multiOrientedSheetMap();
        }
        return excelSheetMap;
    }

    @Override
    public List<CellValidationMessage> validate(List<? extends BaseSheet> sheetData, ExcelSheetContext sheetContext) {
        if(sheetData!=null && !sheetData.isEmpty()) {
            return _validate(sheetData.get(0).getClass(), sheetContext.readerContext(), sheetContext);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<CellValidationMessage>> validate(Map<String, List<? extends BaseSheet>> excelData, ExcelSheetContext sheetContext) {
        return _validate(null, excelData, sheetContext);
    }

    @Override
    public void readAndValidate(Class<? extends BaseSheet> clazz, ExcelSheetContext sheetContext) {
        List<CellValidationMessage> validationMessages = _readAndValidate(clazz, sheetContext);
        sheetContext.setSheetValidationMessages(validationMessages);
    }

    @Override
    public void readAndValidate(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) {
        Map<String, List<? extends BaseSheet>> excelSheets = new HashMap<>();
        Map<String, List<CellValidationMessage>> excelValidationMessages = new HashMap<>();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseSheet> clazz = null;
            try {
                clazz = Class.forName(clazzName).asSubclass(BaseSheet.class);
                _readAndValidate(sheetContext, excelSheets, excelValidationMessages, clazz);
            }
            catch (ClassNotFoundException e) {
                if(!sheetContext.suppressExceptions()) {
                    throw new ExcelSheetReadException(e);
                }
            }
        }
        sheetContext.setExcelSheets(excelSheets);
        sheetContext.setExcelValidationMessages(excelValidationMessages);
    }

    @Override
    public void readAndValidate(Class<? extends BaseSheet>[] classes, ExcelSheetContext sheetContext) {
        Map<String, List<? extends BaseSheet>> excelSheets = new HashMap<>();
        Map<String, List<CellValidationMessage>> excelValidationMessages = new HashMap<>();
        for (Class<? extends BaseSheet> clazz: classes) {
            _readAndValidate(sheetContext, excelSheets, excelValidationMessages, clazz);
        }
        sheetContext.setExcelSheets(excelSheets);
        sheetContext.setExcelValidationMessages(excelValidationMessages);
    }

    @Override
    public <T extends BaseSheet> ExcelInfo excelInfo(Class<T> clazz, ExcelSheetContext sheetContext) {
        return _excelInfo(clazz, sheetContext);
    }

    @Override
    public ExcelInfo excelInfo(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) {
        ExcelInfo excelInfo = new ExcelInfo();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseSheet> clazz = null;
            try {
                clazz = Class.forName(clazzName).asSubclass(BaseSheet.class);
                ExcelInfo excelInfoItr = _excelInfo(clazz, sheetContext);
                if(excelInfoItr == null) {
                    if(!sheetContext.suppressExceptions()) {
                        throw new ExcelSheetReadException("ExcelInfo is null."); //TODO v2: change to a constant
                    }
                }
                else {
                    excelInfo.sheets().addAll(excelInfoItr.sheets());
                }
            }
            catch (ClassNotFoundException e) {
                if(!sheetContext.suppressExceptions()) {
                    throw new ExcelSheetReadException(e);
                }
            }
        }
        return excelInfo;
    }


    public <T extends BaseSheet> ExcelInfo _excelInfo(Class<T> clazz, ExcelSheetContext context) {
        ExcelSheetReaderContext readerContext =  new ExcelSheetReaderContext();
        readerContext.setSheetName(context.sheetName());
        readerContext.setIgnoreHeaders(context.ignoreHeaders());
        readerContext.setIgnoreHeaderRows(context.ignoreHeaderRows());
        readerContext.setIgnoreHeaderColumns(context.ignoreHeaderColumns());
        readerContext.setSuppressExceptions(context.suppressExceptions());
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        BaseExcelSheetReader sheetReader = _sheetReader(readerContext, clazz, context, multiOrientedSheet!=null);
        if (sheetReader != null) {
            return sheetReader.excelInfo(clazz, readerContext);
        }
        return null;
    }
    private void _resolve(ExcelSheetReaderContext readerContext, ExcelSheetContext context, Map<String, List<? extends BaseSheet>> excelSheets) {
        String sheetName = readerContext.sheetName();
        boolean multiOriented = readerContext.extractMultiOrientedMap();
        context.readerContexts().put(sheetName, readerContext);
        if(multiOriented) {
            Map<String, List<? extends BaseSheet>> multiOrientedSheetMap = readerContext.multiOrientedSheetMap();
            List<? extends BaseSheet> multiOrientedSheetData = multiOrientedSheetMap.entrySet().stream().flatMap(e-> e.getValue().stream()).collect(Collectors.toList());
            excelSheets.put(sheetName, multiOrientedSheetData);
        }
        else {
            excelSheets.put(sheetName, readerContext.getSheetData());
        }
        context.setExcelSheets(excelSheets);
    }

    private void _readAndResolve(String[] fullyQualifiedClassNames, ExcelSheetContext context, Map<String, List<? extends BaseSheet>> excelSheets) {
        if(concurrent) {
            _concurrentRead(fullyQualifiedClassNames, context, excelSheets);
        }
        else {
            for (String clazzName : fullyQualifiedClassNames) {
                Class<? extends BaseSheet> clazz = null;
                try {
                    clazz = Class.forName(clazzName).asSubclass(BaseSheet.class);
                    ExcelSheetReaderContext readerContext = _read(context, clazz);
                    _resolve(readerContext, context, excelSheets);
                }
                catch (ClassNotFoundException e) {
                    if(!context.suppressExceptions()) {
                        throw new ExcelSheetReadException(e);
                    }
                }
            }
        }
    }

    private void _concurrentRead(String[] fullyQualifiedClassNames, ExcelSheetContext context, Map<String, List<? extends BaseSheet>> excelSheets) {
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        List<Future<ExcelSheetReaderContext>> futureList = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseSheet> clazz = null;
            try {
                clazz = Class.forName(clazzName).asSubclass(BaseSheet.class);
            }
            catch (ClassNotFoundException e) {
                if(!context.suppressExceptions()) {
                    throw new ExcelSheetReadException(e);
                }
            }
            ExcelSheetReaderTask task = new ExcelSheetReaderTask(TaskType.READ_MULTIPLE_SHEETS.name(), null, service, context, clazz);
            taskCallables.add(task);
        }
        try {
            futureList = executor.invokeAll(taskCallables);
        }
        catch (InterruptedException e) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException(e);
            }
        }
        if(futureList!=null) {
            for(Future<ExcelSheetReaderContext> futureContext : futureList) {
                ExcelSheetReaderContext readerContext = null;
                try {
                    readerContext = futureContext.get();
                    _resolve(readerContext, context, excelSheets);
                }
                catch (InterruptedException | ExecutionException e) {
                    if(!context.suppressExceptions()) {
                        throw new ExcelSheetReadException(e);
                    }
                }
            }
        }
        executor.shutdown();
    }

    protected ExcelSheetReaderContext _read(ExcelSheetContext context, Class<? extends BaseSheet> clazz) {
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

    private  <T extends BaseSheet> ExcelSheetReaderContext _read(Class<T> clazz, ExcelSheetContext context, boolean multiOriented) {
        ExcelSheetReaderContext readerContext =  new ExcelSheetReaderContext();
        readerContext.setSheetName(context.sheetName());
        readerContext.setIgnoreHeaders(context.ignoreHeaders());
        readerContext.setIgnoreHeaderRows(context.ignoreHeaderRows());
        readerContext.setIgnoreHeaderColumns(context.ignoreHeaderColumns());
        readerContext.setSuppressExceptions(context.suppressExceptions());
        BaseExcelSheetReader sheetReader = _sheetReader(readerContext, clazz, context, multiOriented);
        if (sheetReader != null) {
            sheetReader.read(clazz, readerContext);
        }
        return readerContext;
    }

    private  <T extends BaseSheet> BaseExcelSheetReader _sheetReader(ExcelSheetReaderContext readerContext, Class<T> clazz, ExcelSheetContext context, boolean multiOriented) {
        File excelFile = context.excelFile();
        readerContext.setWorkbook(context.workbook());
        if(excelFile !=null && context.workbook() == null) {
            FileInputStream inputS = null;
            try {
                inputS = new FileInputStream(excelFile);
                readerContext.setWorkbook(ExcelSheetReaderUtil.generateWorkbook(inputS, excelFile.getAbsolutePath()));
                context.setFileName(excelFile.getName());
            }
            catch (Exception e) {
                if(!context.suppressExceptions()) {
                    throw new ExcelSheetReadException(e);
                }
            }
        }
        readerContext.setFileName(context.fileName());
        readerContext.setExtractMultiOrientedMap(multiOriented);
        return ExcelSheetFactory.generateReader(clazz, concurrent, batchSize);
    }

    private List<CellValidationMessage> _readAndValidate(Class<? extends BaseSheet> clazz, ExcelSheetContext sheetContext) {
        ExcelSheetReaderContext readerContext = _read(clazz, sheetContext, false);
        sheetContext.setSheet(readerContext.getSheetData());
        return _validate(clazz, readerContext, sheetContext);
    }

    private void _readAndValidate(ExcelSheetContext sheetContext, Map<String, List<? extends BaseSheet>> excelSheets, Map<String, List<CellValidationMessage>> excelValidationMessages, Class<? extends BaseSheet> clazz) {
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        boolean multiOriented = multiOrientedSheet != null;
        ExcelSheetReaderContext readerContext = _read(clazz, sheetContext, multiOriented);
        if(multiOriented) {
            Map<String, List<? extends BaseSheet>> multiOrientedSheetMap = readerContext.multiOrientedSheetMap();
            List<? extends BaseSheet> multiOrientedSheetData = multiOrientedSheetMap.entrySet().stream().flatMap(e-> e.getValue().stream()).collect(Collectors.toList());
            excelSheets.put(multiOrientedSheet.name(), multiOrientedSheetData);
            sheetContext.setReaderContexts(readerContext.multiOrientedReaderContexts());
            Map<String, List<CellValidationMessage>> validationMessageMap = _validate(multiOrientedSheet, multiOrientedSheetMap, sheetContext);
            List<CellValidationMessage> validationMessages = validationMessageMap.entrySet().stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
            if(!validationMessages.isEmpty()) {
                excelValidationMessages.put(multiOrientedSheet.name(), validationMessages);
            }
        }
        else {
            List<? extends BaseSheet> sheetData = readerContext.getSheetData();
            excelSheets.put(sheet.value(), sheetData);
            List<CellValidationMessage> validationMessages = _validate(clazz, readerContext, sheetContext);
            if(!validationMessages.isEmpty()) {
                excelValidationMessages.put(sheet.value(), validationMessages);
            }
        }
    }

    private Map<String, List<CellValidationMessage>> _validate(MultiOrientedSheet multiOrientedSheet, Map<String, List<? extends BaseSheet>> excelData, ExcelSheetContext sheetContext) {
        Map<String, List<CellValidationMessage>> excelValidationMessages = new HashMap<>();
        excelData.forEach((headerKey, excelSheet) -> {
            ExcelSheetReaderContext readerContext = sheetContext.readerContexts().get(headerKey);
            readerContext.setSheetData(excelSheet);
            List<CellValidationMessage> validationMessages = _validate(multiOrientedSheet, excelSheet.get(0).getClass(), readerContext, sheetContext);
            if(!validationMessages.isEmpty()) {
                excelValidationMessages.put(headerKey, validationMessages);
            }
        });
        return excelValidationMessages;
    }

    private List<CellValidationMessage> _validate(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext readerContext, ExcelSheetContext sheetContext) {
        return _validate(null, clazz, readerContext, sheetContext);
    }

    private List<CellValidationMessage> _validate(MultiOrientedSheet multiOrientedSheet, Class<? extends BaseSheet> clazz, ExcelSheetReaderContext readerContext, ExcelSheetContext sheetContext) {
        CellValidatorContext validatorContext = new CellValidatorContext();
        validatorContext.setSheetContext(sheetContext);
        validatorContext.setReaderContext(readerContext);
        if(multiOrientedSheet!=null) {
            validatorContext.setSheetName(multiOrientedSheet.name());
        }
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        List<? extends BaseSheet> rows = readerContext.getSheetData();
        if(sheet.dynamicHeaders()) {
            return Collections.emptyList();
        }
        return ExcelSheetValidator
                .init(validatorContext)
                .setUserValidatorMap(sheetContext.userValidatorMap())
                .setValidValuesDataSet(sheetContext.validValuesDataSet())
                .setUserDefinedMessageDataSet(sheetContext.userDefinedMessageDataSet())
                .rows(rows)
                .validate();
    }
}
