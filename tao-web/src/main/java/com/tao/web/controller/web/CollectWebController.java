package com.tao.web.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.common.entity.AjaxResult;
import com.tao.goods.service.CollectService;
import com.tao.user.entity.User;
import com.tao.user.service.UserService;
import com.tao.web.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端收藏接口（/web/collect/** 已在 WebConfig 中配置需要登录）
 */
@RestController
@RequestMapping("/web/collect")
public class CollectWebController {

    @Autowired
    private CollectService collectService;

    @Autowired
    private UserService userService;

    /**
     * 收藏商品
     */
    @PostMapping("/add/{goodsId}")
    public AjaxResult add(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        return collectService.collect(user.getId(), goodsId)
                ? AjaxResult.success("收藏成功", true)
                : AjaxResult.error("收藏失败", false);
    }

    /**
     * 取消收藏（逻辑删除）
     */
    @DeleteMapping("/cancel/{goodsId}")
    public AjaxResult cancel(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        return collectService.cancel(user.getId(), goodsId)
                ? AjaxResult.success("已取消收藏", true)
                : AjaxResult.error("取消收藏失败", false);
    }

    /**
     * 我收藏的商品ID列表
     */
    @GetMapping("/my")
    public AjaxResult my(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        return AjaxResult.success(collectService.myGoodsIds(user.getId()));
    }

    /**
     * 判断是否已收藏某商品
     */
    @GetMapping("/check/{goodsId}")
    public AjaxResult check(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("collected", collectService.isCollected(user.getId(), goodsId));
        return AjaxResult.success(data);
    }

    /**
     * 取当前登录用户
     */
    private User currentUser(HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        if (userName == null) {
            return null;
        }
        return userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
    }
}