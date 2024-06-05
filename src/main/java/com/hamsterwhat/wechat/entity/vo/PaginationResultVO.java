package com.hamsterwhat.wechat.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaginationResultVO<T> {

    private Integer totalCount;

    private Integer currentPage;

    private Integer pageSize;

    private Integer totalPage;

    private List<T> list = new ArrayList<T>();

    public PaginationResultVO() {}

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage == 0 ? 1 : totalPage;
    }
}
