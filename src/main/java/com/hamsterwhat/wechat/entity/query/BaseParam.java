package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseParam {

    private String orderBy;

    private String orderDirection;

    private Integer offset;

    private Integer limit;

    private Integer page;

    private Integer pageSize;

    public void setPage(Integer page) {
        if (page == null) {
            this.page = null;
        } else if (page <= 0) {
            this.page = 1;
        } else {
            this.page = page;
        }

    }

    public Integer getLimit() {
        if (page == null || pageSize == null) {
            return null;
        }
        return pageSize;
    }

    public Integer getOffset() {
        if (page == null || pageSize == null) {
            return null;
        }
        return (page - 1) * pageSize;
    }

    /**
     * SQL Joining keyword
     */
    @Getter
    public enum TableJoinType {
        LEFT,
        RIGHT,
        INNER,
        FULL;
    }
}
