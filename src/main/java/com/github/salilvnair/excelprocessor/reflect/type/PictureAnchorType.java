package com.github.salilvnair.excelprocessor.reflect.type;

public enum PictureAnchorType {
	MOVE_AND_RESIZE(0),
	MOVE_DONT_RESIZE(2),
	DONT_MOVE_AND_RESIZE(3);
	private final int anchorType;
	public int value(){
		return anchorType;
	}
	PictureAnchorType(int anchorType){
		this.anchorType = anchorType;
	}

}
