package com.github.salilvnair.excelprocessor.v2.model;

import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
@Setter
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

    public int totalRows() {
        return totalRows;
    }

    public int totalColumns() {
        return totalColumns;
    }

    public List<CellInfo> cells() {
        return cells;
    }

    public int valueRowIndex() {
        return valueRowIndex;
    }

    public int valueColumnIndex() {
        return valueColumnIndex;
    }

    public int headerRowAt() {
        return headerRowAt;
    }

    public String headerColumnAt() {
        return headerColumnAt;
    }

    public static SheetInfoBuilder builder() {
        return new SheetInfoBuilder();
    }

    public boolean vertical() {
        return vertical;
    }

    public String[] ignoreHeaderPatterns() {
        return ignoreHeaderPatterns.toArray(new String[0]);
    }

    public boolean skipGettersAndSetters() {
        return skipGettersAndSetters;
    }

    public boolean allCellTypeToString() {
        return allCellTypeToString;
    }

    public boolean useOriginalHeader() {
        return useOriginalHeader;
    }

    public boolean sectional() {
        return sectional;
    }

    public int headerRowEndsAt() {
        return headerRowEndsAt;
    }

    public String headerColumnEndsAt() {
        return headerColumnEndsAt;
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
