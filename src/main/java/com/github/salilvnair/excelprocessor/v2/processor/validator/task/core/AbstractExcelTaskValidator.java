package com.github.salilvnair.excelprocessor.v2.processor.validator.task.core;

public abstract class AbstractExcelTaskValidator implements ExcelTaskValidator {
	private String methodName;

	public String getMethodName() {
		return this.methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
		
}
