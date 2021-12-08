package com.github.salilvnair.excelprocessor.v2.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;

import java.util.Map;

/**
 * @author Salil V Nair
 */
public class DynamicHeaderSheet extends BaseSheet {
    @DynamicCell
    private Map<String, Object> dynamicHeaderKeyedCellValueMap;

    public Map<String, Object> dynamicHeaderKeyedCellValueMap() {
        return dynamicHeaderKeyedCellValueMap;
    }

    public void setDynamicHeaderKeyedCellValueMap(Map<String, Object> dynamicHeaderKeyedCellValueMap) {
        this.dynamicHeaderKeyedCellValueMap = dynamicHeaderKeyedCellValueMap;
    }
}
