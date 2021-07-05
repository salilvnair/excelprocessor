package com.github.salilvnair.excelprocessor.reflect.service;

public abstract class AbstractCustomValidatorTask implements ICustomValidatorTask{
	private String methodName;

	public String getMethodName() {
		return this.methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
		
}
