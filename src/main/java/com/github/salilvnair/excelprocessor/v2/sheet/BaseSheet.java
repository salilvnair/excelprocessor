package com.github.salilvnair.excelprocessor.v2.sheet;

import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseSheet implements ExcelSheet {
    private int rowIndex;
    private int columnIndex;
    private int row;
    private String column;
    private Map<String, CellInfo> cells;
    private List<String> sheetHeaders;

    //getters and setters
    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setCells(Map<String, CellInfo> cells) {
        this.cells = cells;
    }

    public void setSheetHeaders(List<String> sheetHeaders) {
        this.sheetHeaders = sheetHeaders;
    }

    //util functions
    public Map<String, CellInfo> cells() {
        if(cells == null) {
            cells = new LinkedHashMap<>();
        }
        return cells;
    }

    public List<String> sheetHeaders() {
        return sheetHeaders;
    }

    public String[] rowForegroundHex() {
        if(!cells.isEmpty()) {
            List<String> hexStrings = rowForegroundHexList();
            return hexStrings.toArray(new String[0]);
        }
        return null;
    }

    private List<String> rowForegroundHexList() {
        List<String> hexStrings = new ArrayList<>();
        if(!cells.isEmpty()) {
            cells.forEach((key, value) -> {
                String hexString = value.getForegroundHexString();
                if (hexString != null) {
                    hexStrings.add(hexString);
                }
            });
        }
        return hexStrings;
    }

    public String foregroundHex() {
        List<String> foregroundHexList = rowForegroundHexList();
        if(!foregroundHexList.isEmpty()) {
            Set<String> distinctHex = foregroundHexList
                                        .stream()
                                        .map(String::toString)
                                        .collect(Collectors.toSet());
            if(distinctHex.size() == 1) {
                return foregroundHexList.get(0);
            }
        }
        return null;
    }

    public List<String> rowBackgroundHexList() {
        List<String> hexStrings = new ArrayList<>();
        if(!cells.isEmpty()) {
            cells.forEach((key, value) -> {
                String hexString = value.getBackgroundHexString();
                if (hexString != null) {
                    hexStrings.add(hexString);
                }
            });
        }
        return hexStrings;
    }

    public String[] rowBackgroundHex() {
        if(!cells.isEmpty()) {
            List<String> hexStrings = rowBackgroundHexList();
            return hexStrings.toArray(new String[0]);
        }
        return null;
    }

    public String backgroundHex() {
        List<String> backgroundHexList = rowBackgroundHexList();
        if(!backgroundHexList.isEmpty()) {
            Set<String> distinctHex = backgroundHexList
                                        .stream()
                                        .map(String::toString)
                                        .collect(Collectors.toSet());
            if(distinctHex.size() == 1) {
                return backgroundHexList.get(0);
            }
        }
        return null;
    }

    public List<short[]> rowForegroundRgb() {
        if(!cells.isEmpty()) {
            List<short[]> hexRgbData = new ArrayList<short[]>() {
                @Override
                public String toString() {
                    return this.stream().map(Arrays::toString).collect(Collectors.joining(","));
                }
            };
            cells.forEach((key, value) -> {
                short[] hexRgb = value.getForegroundRgb();
                if (hexRgb != null) {
                    hexRgbData.add(hexRgb);
                }
            });
            return hexRgbData;
        }
        return null;
    }

    public short[] foregroundRgb() {
        List<short[]> rowForegroundRgb = rowForegroundRgb();
        if(rowForegroundRgb !=null && !rowForegroundRgb.isEmpty()) {
            Set<String> distinctRgbs = rowForegroundRgb
                                            .stream()
                                            .map(Arrays::toString)
                                            .collect(Collectors.toSet());
            if(distinctRgbs.size() == 1) {
                return rowForegroundRgb.get(0);
            }
        }
        return null;
    }

    public List<short[]> rowBackgroundHexRgb() {
        if(!cells.isEmpty()) {
            List<short[]> hexRgbData = new ArrayList<short[]>() {
                @Override
                public String toString() {
                    return this.stream().map(Arrays::toString).collect(Collectors.joining(","));
                }
            };
            cells.forEach((key, value) -> {
                short[] hexRgb = value.getBackgroundRgb();
                if (hexRgb != null) {
                    hexRgbData.add(hexRgb);
                }
            });
            return hexRgbData;
        }
        return null;
    }

    public short[] backgroundRgb() {
        List<short[]> rowBackgroundHexRgb = rowBackgroundHexRgb();
        if(rowBackgroundHexRgb !=null && !rowBackgroundHexRgb.isEmpty()) {
            Set<String> distinctRgbs = rowBackgroundHexRgb
                                        .stream()
                                        .map(Arrays::toString)
                                        .collect(Collectors.toSet());
            if(distinctRgbs.size() == 1) {
                return rowBackgroundHexRgb.get(0);
            }
        }
        return null;
    }
}
