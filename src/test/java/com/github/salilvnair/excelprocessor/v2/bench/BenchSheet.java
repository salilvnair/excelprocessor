package com.github.salilvnair.excelprocessor.v2.bench;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.BorderStyle;

@Getter
@Setter
@Sheet("Bench")
@DataCellStyle(hasBorderStyle = true, borderStyle = BorderStyle.THIN)
public class BenchSheet extends BaseSheet {
    @Cell("Col0") private String col0;
    @Cell("Col1") private Double col1;
    @Cell("Col2") private String col2;
    @Cell("Col3") private Long col3;
    @Cell("Col4") private String col4;
    @Cell("Col5") private Double col5;
    @Cell("Col6") private String col6;
    @Cell("Col7") private Integer col7;
    @Cell("Col8") private String col8;
    @Cell("Col9") private Double col9;

    public static BenchSheet row(int i) {
        BenchSheet s = new BenchSheet();
        s.col0 = "name-" + i; s.col1 = i * 1.5; s.col2 = "city-" + (i % 100); s.col3 = (long) i;
        s.col4 = "dept-" + (i % 7); s.col5 = i / 3.0; s.col6 = "x" + i; s.col7 = i % 1000;
        s.col8 = "note"; s.col9 = i * 0.25;
        return s;
    }
}
