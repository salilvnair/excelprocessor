package com.github.salilvnair.excelprocessor.v2.test.sheet.merged;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;

/**
 * @author Salil V Nair
 */
public class RankInfoMergedHeader {
    @Cell("Rank")
    private Integer rank;
    @Cell("Affiliated")
    private String affiliated;
    @Cell("All India Rank")
    private Integer allIndiaRank;


    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getAffiliated() {
        return affiliated;
    }

    public void setAffiliated(String affiliated) {
        this.affiliated = affiliated;
    }

    public Integer getAllIndiaRank() {
        return allIndiaRank;
    }

    public void setAllIndiaRank(Integer allIndiaRank) {
        this.allIndiaRank = allIndiaRank;
    }
}
