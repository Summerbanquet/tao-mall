package com.campustao.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_goods")
public class Goods {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 发布者用户ID（t_user.id） */
    private Long userId;

    private Long categoryId;

    private String title;

    private String description;

    /** 金额永远用 BigDecimal，不用 double（精度问题） */
    private BigDecimal price;

    private String coverUrl;

    /** 1在售 2下架 3已售 */
    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}