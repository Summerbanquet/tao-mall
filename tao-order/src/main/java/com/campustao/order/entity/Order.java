package com.campustao.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单号：业务层生成，不是数据库生成 */
    private String orderNo;

    private Long goodsId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    /** 0待付款 1已完成 2已取消 */
    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}