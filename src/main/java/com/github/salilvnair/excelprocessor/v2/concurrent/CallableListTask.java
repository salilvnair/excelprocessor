
package com.github.salilvnair.excelprocessor.v2.concurrent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CallableListTask<T> implements Callable<List<T>>{

	private final Map<String, Object> taskParams;

	private ConcurrentTaskService<T> taskService;

	private final String taskType;

	public CallableListTask(String taskType, Map<String, Object> taskParams, ConcurrentTaskService<T> taskService) {
		this.taskParams = taskParams;
		this.taskService = taskService;
		this.taskType = taskType;
	}
	

	@Override
	public List<T> call() throws Exception {
		 return taskService.toList(this.taskType, this.taskParams);
	}

	public void setTaskService(ConcurrentTaskService<T> taskService) {
		this.taskService = taskService;
	}
}
