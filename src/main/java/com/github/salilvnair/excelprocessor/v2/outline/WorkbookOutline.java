package com.github.salilvnair.excelprocessor.v2.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * What a file contains, without mapping it to a class: sheets in workbook order with their headers,
 * row counts and inferred column types. Built by {@code ExcelSheetReaderFactory.outline(...)}.
 */
public final class WorkbookOutline {
    private final List<SheetOutline> sheets;

    public WorkbookOutline(List<SheetOutline> sheets) {
        this.sheets = Collections.unmodifiableList(new ArrayList<>(sheets));
    }

    public List<SheetOutline> sheets() {
        return sheets;
    }

    public List<String> sheetNames() {
        List<String> names = new ArrayList<>(sheets.size());
        for (SheetOutline sheet : sheets) {
            names.add(sheet.name());
        }
        return names;
    }

    public SheetOutline sheet(String name) {
        for (SheetOutline sheet : sheets) {
            if (sheet.name().equals(name)) {
                return sheet;
            }
        }
        throw new NoSuchElementException("No sheet named '" + name + "'; sheets: " + sheetNames());
    }

    @Override
    public String toString() {
        return "WorkbookOutline" + sheets;
    }
}
