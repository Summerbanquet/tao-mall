package com.tao.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tao.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单表 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}