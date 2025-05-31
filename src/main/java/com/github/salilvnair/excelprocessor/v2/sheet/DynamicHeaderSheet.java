package com.github.salilvnair.excelprocessor.v2.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class DynamicHeaderSheet extends BaseSheet {
    @DynamicCell
    private LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap;
    private LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap;

    public LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap() {
        return dynamicHeaderKeyedCellValueMap;
    }

    public void setDynamicHeaderKeyedCellValueMap(LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap) {
        this.dynamicHeaderKeyedCellValueMap = dynamicHeaderKeyedCellValueMap;
    }

    public LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap() {
        return dynamicHeaderKeyedCellInfoMap;
    }

    public void setDynamicHeaderKeyedCellInfoMap(LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap) {
        this.dynamicHeaderKeyedCellInfoMap = dynamicHeaderKeyedCellInfoMap;
    }


    public Map<String, Object> extract() {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        cells().forEach((k,v) -> {
            String header = v.originalHeader();
            if(valueMap.containsKey(header)) {
                valueMap.put(k, dynamicHeaderKeyedCellValueMap.get(k));
            }
            else {
                valueMap.put(header, dynamicHeaderKeyedCellValueMap.get(k));
            }
        });
        return valueMap;
    }

    public Map<String, List<CellInfo>> extractCells() {
        Map<String, List<CellInfo>> valueMap = new LinkedHashMap<>();
        cells().forEach((k,v) -> {
            String header = v.originalHeader();
            if(valueMap.containsKey(header)) {
                List<CellInfo> cellInfos = valueMap.get(header);
                cellInfos.add(v);
                valueMap.put(header, cellInfos);
            }
            else {
                List<CellInfo> cellInfos = new ArrayList<>();
                cellInfos.add(v);
                valueMap.put(header, cellInfos);
            }
        });
        return valueMap;
    }
}
