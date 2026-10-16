package com.tao.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表映射实体类
 */
@Data
@TableName("t_order")
public class Order implements Serializable {

    /** 订单状态：待付款 */
    public static final int STATUS_PENDING_PAY = 0;
    /** 订单状态：已付款 */
    public static final int STATUS_PAID = 1;
    /** 订单状态：已发货 */
    public static final int STATUS_SHIPPED = 2;
    /** 订单状态：已完成 */
    public static final int STATUS_COMPLETED = 3;
    /** 订单状态：已取消 */
    public static final int STATUS_CANCELLED = 4;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 用户名（冗余字段）
     */
    private String userName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品单价
     */
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 收货人
     */
    private String receiver;

    /**
     * 收货手机号
     */
    private String phone;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单状态(0待付款 1已付款 2已发货 3已完成 4已取消)
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}