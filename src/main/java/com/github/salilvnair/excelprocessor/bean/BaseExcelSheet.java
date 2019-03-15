package com.github.salilvnair.excelprocessor.bean;

public abstract class BaseExcelSheet implements IExcelSheet {
	private int rowNum;
	private int excelRowIndex;
	private String columnName="A";
	public int getRowNum() {
		return rowNum;
	}
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	public int getExcelRowIndex() {
		return excelRowIndex;
	}
	public void setExcelRowIndex(int excelRowIndex) {
		this.excelRowIndex = excelRowIndex;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
}
