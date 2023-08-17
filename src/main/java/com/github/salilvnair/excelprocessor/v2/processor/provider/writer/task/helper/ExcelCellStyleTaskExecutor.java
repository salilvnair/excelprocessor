package com.github.salilvnair.excelprocessor.v2.processor.provider.writer.task.helper;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;

/**
 * @author Salil V Nair
 */
public class ExcelCellStyleTaskExecutor {
    public static Object execute(String methodName, Class<? extends AbstractExcelTask> taskClass, ExcelSheetWriterContext writerContext) {
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName())) {
            try {
                AbstractExcelTask task = taskClass.newInstance();
                task.setMethodName(methodName);
                return ReflectionUtil.invokeMethod(task, task.getMethodName(), writerContext);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
