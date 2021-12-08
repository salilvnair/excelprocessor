package com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;

/**
 * @author Salil V Nair
 */
public class ExcelValidatorTaskExecutor {
    public static Object execute(String methodName, Class<? extends AbstractExcelTaskValidator> taskClass, CellValidatorContext validatorContext) {
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName())) {
            try {
                AbstractExcelTaskValidator task = taskClass.newInstance();
                task.setMethodName(methodName);
                return ReflectionUtil.invokeMethod(task, task.getMethodName(), validatorContext);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
