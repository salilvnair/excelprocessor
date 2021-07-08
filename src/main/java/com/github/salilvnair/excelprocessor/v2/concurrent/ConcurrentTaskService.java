package com.github.salilvnair.excelprocessor.v2.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ConcurrentTaskService<T> {
    default List<T> toList(String taskType, Map<String, Object> taskParams) { return Collections.emptyList();};
    default Map<String,List<T>> toMap(String taskType, Map<String, Object> taskParams) { return Collections.emptyMap(); };
    default T toContext(String taskType, Map<String, Object> taskParams, Object...args) { return null; };
}
