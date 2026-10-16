package com.tao.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总行数
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<?> rows;

    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.rows = list;
    }
}
