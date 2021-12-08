package com.github.salilvnair.excelprocessor.v2.processor.concurrent.task;

import com.github.salilvnair.excelprocessor.v2.concurrent.CallableContextTask;
import com.github.salilvnair.excelprocessor.v2.concurrent.ConcurrentTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;

import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderTask extends CallableContextTask<ExcelSheetReaderContext> {

    public ExcelSheetReaderTask(String taskType, Map<String, Object> taskParams, ConcurrentTaskService<ExcelSheetReaderContext> taskService, Object... args) {
        super(taskType, taskParams, taskService, args);
    }
}
