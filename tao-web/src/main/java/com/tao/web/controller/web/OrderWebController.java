package com.tao.web.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.common.entity.AjaxResult;
import com.tao.common.entity.PageResult;
import com.tao.order.entity.Order;
import com.tao.order.entity.query.OrderQuery;
import com.tao.order.service.OrderService;
import com.tao.user.entity.User;
import com.tao.user.service.UserService;
import com.tao.web.exception.BusinessException;
import com.tao.web.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单前端控制器：用户下单、查询我的订单、取消订单
 */
@RestController
@RequestMapping("/web/order")
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * 用户下单
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody Order form, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        if (userName == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (form.getGoodsName() == null || form.getGoodsPrice() == null) {
            return AjaxResult.error("商品信息不完整");
        }
        int qty = form.getQuantity() == null ? 1 : form.getQuantity();
        BigDecimal total = form.getGoodsPrice().multiply(new BigDecimal(qty));

        Order order = new Order();
        order.setOrderNo("OD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setUserId(user.getId());
        order.setUserName(user.getUserName());
        order.setGoodsName(form.getGoodsName());
        order.setGoodsPrice(form.getGoodsPrice());
        order.setQuantity(qty);
        order.setTotalAmount(total);
        order.setReceiver(form.getReceiver());
        order.setPhone(form.getPhone());
        order.setAddress(form.getAddress());
        order.setRemark(form.getRemark());
        order.setStatus(Order.STATUS_PENDING_PAY);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        boolean ok = orderService.save(order);
        return ok ? AjaxResult.success("下单成功", order) : AjaxResult.error("下单失败", false);
    }

    /**
     * 分页查询我的订单
     */
    @PostMapping("/page")
    public AjaxResult page(@RequestBody OrderQuery query, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        if (userName == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        query.setUserId(user.getId());
        PageResult<Order> page = orderService.page(query);
        return AjaxResult.success(page);
    }

    /**
     * 查看订单详情（仅限自己的订单）
     */
    @GetMapping("/getInfo/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        Order order = orderService.getById(id);
        if (order == null) {
            return AjaxResult.error("订单不存在");
        }
        if (!userName.equals(order.getUserName())) {
            return AjaxResult.error("无权访问该订单");
        }
        return AjaxResult.success(order);
    }

    /**
     * 取消订单（仅限待付款状态）
     */
    @PostMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable("id") Long id, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        boolean ok = orderService.cancel(id, user.getId());
        return ok ? AjaxResult.success("取消成功", true) : AjaxResult.error("取消失败，订单状态不可取消");
    }

    /**
     * 买家付款（待付款 -> 已付款）
     */
    @PostMapping("/pay/{id}")
    public AjaxResult pay(@PathVariable("id") Long id, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        boolean ok = orderService.pay(id, user.getId());
        return ok ? AjaxResult.success("付款成功", true) : AjaxResult.error("付款失败，仅待付款订单可付款");
    }

    /**
     * 买家确认收货（已发货 -> 已完成）
     */
    @PostMapping("/confirm/{id}")
    public AjaxResult confirm(@PathVariable("id") Long id, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        boolean ok = orderService.confirm(id, user.getId());
        return ok ? AjaxResult.success("已确认收货", true) : AjaxResult.error("操作失败，仅已发货订单可确认收货");
    }
}