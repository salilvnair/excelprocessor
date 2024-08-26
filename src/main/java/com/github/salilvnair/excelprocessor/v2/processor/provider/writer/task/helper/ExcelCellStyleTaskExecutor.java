package com.github.salilvnair.excelprocessor.v2.processor.provider.writer.task.helper;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Salil V Nair
 */
public class ExcelCellStyleTaskExecutor {
    public static Object execute(String methodName, Class<? extends AbstractExcelTask> taskClass, ExcelSheetWriterContext writerContext) {
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName()) || writerContext.getTaskBean()!=null) {
            try {
                AbstractExcelTask task = writerContext.getTaskBean()!=null ? writerContext.getTaskBean() : taskClass.newInstance();
                task.setMethodName(methodName);
                if(CollectionUtils.isNotEmpty(writerContext.taskMetadata())) {
                    return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext, writerContext.taskMetadata().toArray(new Object[0]));
                }
                return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext);
            }
            catch (Exception ex) {
                if(!writerContext.suppressTaskExceptions()) {
                    throw new ExcelSheetWriterException(ex);
                }
            }
        }
        return null;
    }

    public static Object execute(String methodName, Sheet sheet, ExcelSheetWriterContext writerContext) {
        Class<? extends AbstractExcelTask> taskClass = sheet.excelTask();
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName()) || writerContext.getTaskBean()!=null || !"".equals(sheet.excelTaskBeanName())) {
            try {
                AbstractExcelTask task;
                String beanName = sheet.excelTaskBeanName();
                if(beanName !=null && !beanName.isEmpty()) {
                    task = (AbstractExcelTask) writerContext.beanFunction().apply(beanName);
                    if (task == null) {
                        task = taskClass.newInstance();
                    }
                }
                else {
                    task = writerContext.getTaskBean()!=null ? writerContext.getTaskBean() : taskClass.newInstance();
                }
                task.setMethodName(methodName);
                if(CollectionUtils.isNotEmpty(writerContext.taskMetadata())) {
                    return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext, writerContext.taskMetadata().toArray(new Object[0]));
                }
                return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext);
            }
            catch (Exception ex) {
                if(!writerContext.suppressTaskExceptions()) {
                    throw new ExcelSheetWriterException(ex);
                }
            }
        }
        return null;
    }
}
