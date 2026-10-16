package com.tao.goods.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体（对应表 goods）
 */
@Data
@TableName("goods")
public class Goods implements Serializable {

    /** 商品ID */
    private Long id;

    /** 发布者（用户ID） */
    private Long userId;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 商品价格 */
    private BigDecimal price;

    /** 封面图地址 */
    private String cover;

    /** 所属分类ID（对应 category 表） */
    private Long categoryId;

    /** 商品状态（0下架 1在售） */
    private Integer status;

    /** 商品状态：下架 */
    public static final Integer STATUS_OFF = 0;
    /** 商品状态：在售 */
    public static final Integer STATUS_ON = 1;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除（0正常 1已删除），MyBatis-Plus 会自动拼 deleted=0 */
    @TableLogic
    private Integer deleted;
}