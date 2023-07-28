package com.github.salilvnair.excelprocessor.v2.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class DynamicHeaderSheet extends BaseSheet {
    @DynamicCell
    private LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap;

    public LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap() {
        return dynamicHeaderKeyedCellValueMap;
    }

    public void setDynamicHeaderKeyedCellValueMap(LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap) {
        this.dynamicHeaderKeyedCellValueMap = dynamicHeaderKeyedCellValueMap;
    }
}
