package com.tao.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.common.entity.PageResult;
import com.tao.order.entity.Order;
import com.tao.order.entity.query.OrderQuery;
import com.tao.order.mapper.OrderMapper;
import com.tao.order.service.OrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 订单业务实现
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public PageResult<Order> page(OrderQuery query) {
        Page<Order> pageObj = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(query.getOrderNo() != null, Order::getOrderNo, query.getOrderNo())
                .eq(query.getUserId() != null, Order::getUserId, query.getUserId())
                .like(query.getGoodsName() != null, Order::getGoodsName, query.getGoodsName())
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .orderByDesc(Order::getCreateTime);
        Page<Order> page = baseMapper.selectPage(pageObj, wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean cancel(Long orderId, Long userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_PENDING_PAY) {
            return false;
        }
        return baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, Order.STATUS_CANCELLED)
                .set(Order::getUpdateTime, LocalDateTime.now())) > 0;
    }

    @Override
    public boolean pay(Long orderId, Long userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_PENDING_PAY) {
            return false;
        }
        return baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, Order.STATUS_PAID)
                .set(Order::getUpdateTime, LocalDateTime.now())) > 0;
    }

    @Override
    public boolean confirm(Long orderId, Long userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_SHIPPED) {
            return false;
        }
        return baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, Order.STATUS_COMPLETED)
                .set(Order::getUpdateTime, LocalDateTime.now())) > 0;
    }

    @Override
    public boolean updateStatusById(Long id, Integer status) {
        return baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .set(Order::getStatus, status)
                .set(Order::getUpdateTime, LocalDateTime.now())) > 0;
    }

    @Override
    public PageResult<Order> sellPage(OrderQuery query, Long sellerId) {
        Page<Order> pageObj = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, sellerId)
                .eq(query.getOrderNo() != null, Order::getOrderNo, query.getOrderNo())
                .like(query.getGoodsName() != null, Order::getGoodsName, query.getGoodsName())
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .orderByDesc(Order::getCreateTime);
        Page<Order> page = baseMapper.selectPage(pageObj, wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean ship(Long orderId, Long sellerId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !sellerId.equals(order.getSellerId())) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_PAID) {
            return false;
        }
        return baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, Order.STATUS_SHIPPED)
                .set(Order::getUpdateTime, LocalDateTime.now())) > 0;
    }

    @Override
    public boolean complete(Long orderId, Long sellerId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !sellerId.equals(order.getSellerId())) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_SHIPPED) {
            return false;
        }
        return baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, Order.STATUS_COMPLETED)
                .set(Order::getUpdateTime, LocalDateTime.now())) > 0;
    }
}