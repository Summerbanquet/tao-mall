package com.tao.order.entity.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单分页和条件搜索类
 */
@Data
public class OrderQuery implements Serializable {

    /** 订单编号（精确） */
    private String orderNo;

    /** 下单用户ID（精确） */
    private Long userId;

    /** 商品名称（模糊） */
    private String goodsName;

    /** 订单状态 */
    private Integer status;

    /** 分页页码 */
    private Integer pageNum;

    /** 分页大小 */
    private Integer pageSize;
}