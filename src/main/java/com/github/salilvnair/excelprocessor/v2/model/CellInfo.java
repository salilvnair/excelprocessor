package com.github.salilvnair.excelprocessor.v2.model;

import lombok.*;
import org.apache.poi.ss.usermodel.Color;

import java.lang.reflect.Type;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CellInfo {
    public static final String CELL_TYPE_STRING = "String";
    public static final String CELL_TYPE_DOUBLE = "Double";
    public static final String CELL_TYPE_DATE = "Date";
    public static final String CELL_TYPE_BOOLEAN = "Boolean";
    private String originalHeader;
    private String header;
    private Object value;
    //getters and setters
    private int rowIndex;
    private int columnIndex;
    private int row;
    private String column;
    private Type cellType;
    private String cellTypeString;
    private Color backgroundColor;
    private Color foregroundColor;
    private String backgroundHexString;
    private String foregroundHexString;
    private short[] backgroundRgb;
    private short[] foregroundRgb;

    //util functions
    public Object value() {
        return value;
    }

    public int rowIndex() {
        return rowIndex;
    }

    public int columnIndex() {
        return columnIndex;
    }

    public int row() {
        return row;
    }

    public String column() {
        return column;
    }

    public Type cellType() {
        return cellType;
    }

    public String cellTypeString() {
        return cellTypeString;
    }

    public String originalHeader() {
        return originalHeader;
    }

    public String header() {
        return header;
    }

}
