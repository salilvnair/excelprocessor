package com.github.salilvnair.excelprocessor.util;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v1.reflect.service.ICustomValidatorTask;

public class ExcelValidatorUtil {
	private static final Log log = LogFactory.getLog(ExcelValidatorUtil.class);
	
	private String methodName;
	
	private ICustomValidatorTask taskClass;
	
	public  void setTask(ICustomValidatorTask taskClass) {
		this.taskClass = taskClass;
	}
	
	public Object invoke(Object...parameters) {
		if(parameters==null) {
			return null;
		}
		
		Class<?>[] paramString = {};
		if (parameters.length != 0) {
			paramString = new Class[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] != null) {

					paramString[i] = parameters[i].getClass();
				}
			}
		}
		
		Method method = null;
		try {			
			method = taskClass.getClass().getDeclaredMethod(taskClass.getMethodName(), paramString);

			return method.invoke(taskClass, parameters);
		} 
		
		catch (Exception ex) {
			log.error("Exception from ExcelValidatorUtil.invoke>>",ex);			
		}
		return null;
	}

	public Object executeTask(ICustomValidatorTask taskClass,ValidatorContext validatorContext) {
		this.taskClass = taskClass;
		Object obj = invoke(validatorContext);
		return obj;
	}

	public String getMethodName() {
		return methodName;
	}


	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	

}
