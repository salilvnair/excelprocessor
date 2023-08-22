package com.github.salilvnair.excelprocessor.v2.processor.provider.writer.task.helper;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Salil V Nair
 */
public class ExcelCellStyleTaskExecutor {
    public static Object execute(String methodName, Class<? extends AbstractExcelTask> taskClass, ExcelSheetWriterContext writerContext) {
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName())) {
            try {
                AbstractExcelTask task = taskClass.newInstance();
                task.setMethodName(methodName);
                if(CollectionUtils.isNotEmpty(writerContext.taskMetadata())) {
                    return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext, writerContext.taskMetadata().toArray(new Object[0]));
                }
                return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
