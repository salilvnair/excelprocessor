package com.github.salilvnair.excelprocessor.v2.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public class SheetInfo {
    private String name;
    private int headerRowAt=1;
    private int headerRowEndsAt = -1;
    private String headerColumnAt="A";
    private String headerColumnEndsAt="";
    private int totalRows;
    private int totalColumns;
    private int valueRowIndex;
    private int valueColumnIndex;
    private List<CellInfo> cells;
    private boolean vertical;
    private boolean sectional;
    private List<String> ignoreHeaderPatterns = new ArrayList<>();
    private boolean useOriginalHeader;
    private boolean skipGettersAndSetters;
    private boolean allCellTypeToString;

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int totalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int totalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(int totalColumns) {
        this.totalColumns = totalColumns;
    }

    public List<CellInfo> cells() {
        return cells;
    }

    public void setCells(List<CellInfo> cells) {
        this.cells = cells;
    }

    public int valueRowIndex() {
        return valueRowIndex;
    }

    public void setValueRowIndex(int valueRowIndex) {
        this.valueRowIndex = valueRowIndex;
    }

    public int valueColumnIndex() {
        return valueColumnIndex;
    }

    public void setValueColumnIndex(int valueColumnIndex) {
        this.valueColumnIndex = valueColumnIndex;
    }

    public int headerRowAt() {
        return headerRowAt;
    }

    public void setHeaderRowAt(int headerRowAt) {
        this.headerRowAt = headerRowAt;
    }

    public String headerColumnAt() {
        return headerColumnAt;
    }

    public void setHeaderColumnAt(String headerColumnAt) {
        this.headerColumnAt = headerColumnAt;
    }

    public static SheetInfoBuilder builder() {
        return new SheetInfoBuilder();
    }

    public boolean vertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public String[] ignoreHeaderPatterns() {
        return ignoreHeaderPatterns.toArray(new String[0]);
    }

    public void setIgnoreHeaderPatterns(List<String> ignoreHeaderPatterns) {
        this.ignoreHeaderPatterns = ignoreHeaderPatterns;
    }

    public boolean skipGettersAndSetters() {
        return skipGettersAndSetters;
    }

    public void setSkipGettersAndSetters(boolean skipGettersAndSetters) {
        this.skipGettersAndSetters = skipGettersAndSetters;
    }

    public boolean allCellTypeToString() {
        return allCellTypeToString;
    }

    public void setAllCellTypeToString(boolean allCellTypeToString) {
        this.allCellTypeToString = allCellTypeToString;
    }

    public boolean useOriginalHeader() {
        return useOriginalHeader;
    }

    public void setUseOriginalHeader(boolean useOriginalHeader) {
        this.useOriginalHeader = useOriginalHeader;
    }

    public boolean sectional() {
        return sectional;
    }

    public void setSectional(boolean sectional) {
        this.sectional = sectional;
    }

    public int headerRowEndsAt() {
        return headerRowEndsAt;
    }

    public void setHeaderRowEndsAt(int headerRowEndsAt) {
        this.headerRowEndsAt = headerRowEndsAt;
    }

    public String headerColumnEndsAt() {
        return headerColumnEndsAt;
    }

    public void setHeaderColumnEndsAt(String headerColumnEndsAt) {
        this.headerColumnEndsAt = headerColumnEndsAt;
    }

    public static class SheetInfoBuilder {
        private final SheetInfo sheetInfo =  new SheetInfo();

        public SheetInfoBuilder name(String sheetName) {
            sheetInfo.setName(sheetName);
            return this;
        }

        public SheetInfoBuilder headerRowAt(int headerRowAt) {
            sheetInfo.setHeaderRowAt(headerRowAt);
            return this;
        }

        public SheetInfoBuilder headerRowEndsAt(int headerRowEndsAt) {
            sheetInfo.setHeaderRowEndsAt(headerRowEndsAt);
            return this;
        }

        public SheetInfoBuilder headerColumnAt(String headerColumnAt) {
            sheetInfo.setHeaderColumnAt(headerColumnAt);
            return this;
        }

        public SheetInfoBuilder headerColumnEndsAt(String headerColumnEndsAt) {
            sheetInfo.setHeaderColumnEndsAt(headerColumnEndsAt);
            return this;
        }

        public SheetInfoBuilder vertical() {
            sheetInfo.setVertical(true);
            return this;
        }

        public SheetInfoBuilder sectional() {
            sheetInfo.setSectional(true);
            return this;
        }

        public SheetInfoBuilder useOriginalHeader() {
            sheetInfo.setUseOriginalHeader(true);
            return this;
        }

        public SheetInfoBuilder skipGettersAndSetters() {
            sheetInfo.setSkipGettersAndSetters(true);
            return this;
        }

        public SheetInfoBuilder allCellTypeToString() {
            sheetInfo.setAllCellTypeToString(true);
            return this;
        }

        public SheetInfoBuilder ignoreHeaderPatterns(String... pattern) {
            List<String> ignoreHeaderPatterns = Arrays.stream(pattern).collect(Collectors.toList());
            sheetInfo.setIgnoreHeaderPatterns(ignoreHeaderPatterns);
            return this;
        }


        public SheetInfo build() {
            return sheetInfo;
        }

    }

}
