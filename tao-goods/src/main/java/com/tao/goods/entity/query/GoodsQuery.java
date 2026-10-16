package com.tao.goods.entity.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品分页和条件搜索类
 */
@Data
public class GoodsQuery implements Serializable {

    /** 商品标题模糊搜索 */
    private String title;

    /** 商品状态（0下架 1在售） */
    private Integer status;

    /** 发布者用户ID（查询"我的商品"时使用） */
    private Long userId;

    /** 分类ID（按分类筛选） */
    private Long categoryId;

    /** 分页页码 */
    private Integer pageNum;

    /** 分页大小 */
    private Integer pageSize;

    @Override
    public String toString() {
        return title + " " + status + " " + userId + " " + pageNum + " " + pageSize;
    }
}