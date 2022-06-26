package com.github.salilvnair.excelprocessor.v2.processor.context;

import lombok.*;
import org.apache.poi.ss.usermodel.Workbook;

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

    private Workbook existingWorkbook;

    private boolean streamingWorkbook;

    private boolean containsExistingWorkbook;

    public Workbook workbook() {
        return super.getWorkbook();
    }

    public boolean streamingWorkbook() {
        return streamingWorkbook;
    }
    public boolean containsExistingWorkbook() {
        return containsExistingWorkbook;
    }

    public Workbook template() {
        return this.template;
    }
    public Workbook existingWorkbook() {
        return this.existingWorkbook;
    }

}
