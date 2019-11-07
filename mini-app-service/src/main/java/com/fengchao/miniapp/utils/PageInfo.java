package com.fengchao.miniapp.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PageInfo<T> {
    private int total;
    private int pageSize;
    private int pageIndex;
    private List<T> rows;

    public PageInfo(int total, int pageSize, int pageIndex, List<T> rows) {
        this.total = total;
        this.rows = rows;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "total=" + total +
                ", pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                ", rows=" + rows +
                '}';
    }
}
