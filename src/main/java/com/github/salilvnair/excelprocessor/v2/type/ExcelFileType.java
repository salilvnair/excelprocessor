package com.github.salilvnair.excelprocessor.v2.type;

public enum ExcelFileType {
    XLS(Extension.XLS),
    XLSX(Extension.XLSX),
    XLSM(Extension.XLSM);
    private final String extension;
    ExcelFileType(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }

    public static class Extension {
        public static final String XLS = "xls";
        public static final String XLSX = "xlsx";
        public static final String XLSM = "xlsm";
    }
}
