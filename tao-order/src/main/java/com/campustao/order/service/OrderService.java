package com.campustao.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campustao.common.result.R;
import com.campustao.order.entity.Order;

public interface OrderService extends IService<Order> {

    /** 下单：生成订单号，落库，返回完整订单 */
    Order createOrder(Order order);

    /** 取消订单：校验存在且待付款，改为已取消 */
    R<Boolean> cancelOrder(Long id);
}