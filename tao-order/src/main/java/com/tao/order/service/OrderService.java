package com.tao.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.common.entity.PageResult;
import com.tao.order.entity.Order;
import com.tao.order.entity.query.OrderQuery;

/**
 * 订单业务接口
 */
public interface OrderService extends IService<Order> {

    /** 分页条件查询 */
    PageResult<Order> page(OrderQuery query);

    /** 取消订单（仅自己的订单，且状态为待付款） */
    boolean cancel(Long orderId, Long userId);
}