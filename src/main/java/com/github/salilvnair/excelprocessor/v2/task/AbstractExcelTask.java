package com.github.salilvnair.excelprocessor.v2.task;

public abstract class AbstractExcelTask implements ExcelTask {
	private String methodName;

	public String getMethodName() {
		return this.methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
		
}
