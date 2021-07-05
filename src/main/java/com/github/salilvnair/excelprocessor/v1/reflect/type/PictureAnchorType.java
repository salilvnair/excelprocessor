package com.github.salilvnair.excelprocessor.v1.reflect.type;

public enum PictureAnchorType {
	MOVE_AND_RESIZE(0),
	MOVE_DONT_RESIZE(2),
	DONT_MOVE_AND_RESIZE(3);
	private int anchorType;
	public int value(){
		return anchorType;
	}
	PictureAnchorType(int anchorType){
		this.anchorType = anchorType;
	}

}
