package com.github.salilvnair.excelprocessor.v2.test.sheet.merged;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;

/**
 * @author Salil V Nair
 */
public class OtherInfoMergedHeader {
    @Cell("Famous")
    private String famous;
    @Cell("Rating")
    private Integer rating;

    public String getFamous() {
        return famous;
    }

    public void setFamous(String famous) {
        this.famous = famous;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
