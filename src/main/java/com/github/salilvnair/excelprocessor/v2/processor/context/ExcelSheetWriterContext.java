package com.github.salilvnair.excelprocessor.v2.processor.context;

import lombok.*;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;

/**
 * @author Salil V Nair
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSheetWriterContext extends BaseExcelSheetContext {

    private Workbook template;

    public Workbook workbook() {
        return super.getWorkbook();
    }

    public Workbook template() {
        return this.template;
    }
}
