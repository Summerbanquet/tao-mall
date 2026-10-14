package com.campustao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustao.common.result.R;
import com.campustao.order.entity.Order;
import com.campustao.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 下单 POST /api/orders （只传 goodsId/buyerId/sellerId/amount，订单号和状态由服务端定） */
    @PostMapping
    public R<Order> create(@RequestBody Order order) {
        return R.ok(orderService.createOrder(order));
    }

    /** 取消订单 PUT /api/orders/{id}/cancel */
    @PutMapping("/{id}/cancel")
    public R<Boolean> cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    /** 分页 GET /api/orders?pageNum=1&pageSize=5 */
    @GetMapping
    public R<Page<Order>> page(@RequestParam(defaultValue = "1") long pageNum,
                               @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(orderService.page(new Page<>(pageNum, pageSize)));
    }

    /** 按买家查 GET /api/orders/buyer/{buyerId} */
    @GetMapping("/buyer/{buyerId}")
    public R<List<Order>> listByBuyer(@PathVariable Long buyerId) {
        return R.ok(orderService.list(
                new LambdaQueryWrapper<Order>().eq(Order::getBuyerId, buyerId)
                        .orderByDesc(Order::getCreateTime)));
    }
}