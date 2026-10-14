package com.campustao.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campustao.common.result.R;
import com.campustao.order.entity.Order;
import com.campustao.order.mapper.OrderMapper;
import com.campustao.order.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public Order createOrder(Order order) {
        // 知识点①：订单号是业务规则（O + 毫秒时间戳 + 3位随机数），写在 Service 而不是 Controller
        String orderNo = "O" + System.currentTimeMillis()
                + (int) ((Math.random() * 9 + 1) * 100);
        order.setOrderNo(orderNo);
        order.setStatus(0); // 待付款
        this.save(order);
        return order;
    }

    @Override
    public R<Boolean> cancelOrder(Long id) {
        // 知识②：改数据前先查（知识点③：状态流转——只有待付款才能取消）
        Order order = this.getById(id);
        if (order == null) {
            return R.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return R.error("当前状态不可取消（仅待付款可取消）");
        }
        order.setStatus(2);
        return R.ok(this.updateById(order));
    }
}