package com.tao.web.controller.adm;

import com.tao.common.entity.AjaxResult;
import com.tao.order.entity.Order;
import com.tao.order.entity.query.OrderQuery;
import com.tao.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 订单管理控制器（管理员端）
 */
@RestController
@RequestMapping("/adm/order")
public class OrderAdmController {

    @Autowired
    private OrderService orderService;

    /**
     * 分页查询所有订单
     */
    @PostMapping("/page")
    public AjaxResult page(@RequestBody OrderQuery query) {
        return AjaxResult.success(orderService.page(query));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/getInfo/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(orderService.getById(id));
    }

    /**
     * 修改订单状态（如发货、标记完成等）
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody Order order) {
        if (order.getId() == null || order.getStatus() == null) {
            return AjaxResult.error("参数不完整");
        }
        boolean ok = orderService.updateStatusById(order.getId(), order.getStatus());
        return ok ? AjaxResult.success("修改成功", true) : AjaxResult.error("修改失败", false);
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/dele/{id}")
    public AjaxResult dele(@PathVariable("id") Long id) {
        return orderService.removeById(id)
                ? AjaxResult.success("删除成功", true)
                : AjaxResult.error("删除失败", false);
    }
}