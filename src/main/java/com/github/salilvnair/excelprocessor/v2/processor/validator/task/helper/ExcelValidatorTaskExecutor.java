package com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;

/**
 * @author Salil V Nair
 */
public class ExcelValidatorTaskExecutor {
    public static Object execute(String methodName, Sheet sheet, CellValidatorContext validatorContext) {
        Class<? extends AbstractExcelTaskValidator> taskClass = sheet.excelTaskValidator();
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName()) || validatorContext.taskValidatorBean()!=null || !"".equals(sheet.excelTaskValidatorBeanName())) {
            try {
                AbstractExcelTaskValidator task;
                String beanName = sheet.excelTaskValidatorBeanName();
                if(beanName!=null && !beanName.isEmpty()) {
                    task = (AbstractExcelTaskValidator) validatorContext.beanResolver().apply(beanName);
                    if (task == null) {
                        task = taskClass.newInstance();
                    }
                }
                else {
                    task = validatorContext.taskValidatorBean()!=null ? validatorContext.taskValidatorBean() : taskClass.newInstance();
                }
                task.setMethodName(methodName);
                return ReflectionUtil.invokeMethod(task, task.getMethodName(), validatorContext);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static Object execute(String methodName, Class<? extends AbstractExcelTaskValidator> taskClass, CellValidatorContext validatorContext) {
        if(!taskClass.getName().equals(Sheet.DefaultTaskValidator.class.getName()) || validatorContext.taskValidatorBean()!=null) {
            try {
                AbstractExcelTaskValidator task = validatorContext.taskValidatorBean()!=null ? validatorContext.taskValidatorBean() : taskClass.newInstance();
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
