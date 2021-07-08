
package com.github.salilvnair.excelprocessor.v2.concurrent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CallableMapTask<T> implements Callable<Map<String,List<T>>>{

	private final Map<String, Object> taskParams;

	private ConcurrentTaskService<T> taskService;

	private final String taskType;

	public CallableMapTask(String taskType, Map<String, Object> taskParams, ConcurrentTaskService<T> taskService) {
		this.taskParams = taskParams;
		this.taskService = taskService;
		this.taskType = taskType;
	}
	

	@Override
	public Map<String,List<T>> call() throws Exception {
		 return taskService.toMap(this.taskType, this.taskParams);
	}

	public void setTaskService(ConcurrentTaskService<T> taskService) {
		this.taskService = taskService;
	}
}
