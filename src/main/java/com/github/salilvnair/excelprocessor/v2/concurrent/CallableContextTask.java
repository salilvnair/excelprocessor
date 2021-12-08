package com.github.salilvnair.excelprocessor.v2.concurrent;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Salil V Nair
 */
public class CallableContextTask<T> implements Callable<T> {
    private final Map<String, Object> taskParams;

    private final ConcurrentTaskService<T> taskService;

    private final String taskType;

    private final Object[] args;

    public CallableContextTask(String taskType, Map<String, Object> taskParams, ConcurrentTaskService<T> taskService, Object...args) {
        this.taskParams = taskParams;
        this.taskService = taskService;
        this.taskType = taskType;
        this.args = args;
    }

    @Override
    public T call() {
        return this.taskService.toContext(taskType, taskParams, args);
    }
}
