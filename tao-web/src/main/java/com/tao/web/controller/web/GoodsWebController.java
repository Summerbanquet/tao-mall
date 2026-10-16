package com.tao.web.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.common.entity.AjaxResult;
import com.tao.goods.entity.Goods;
import com.tao.goods.entity.query.GoodsQuery;
import com.tao.goods.service.GoodsService;
import com.tao.user.entity.User;
import com.tao.user.service.UserService;
import com.tao.web.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户端商品接口
 * 说明：发布/我的商品/删除需要在 WebConfig 中配置登录后才能访问
 */
@RestController
@RequestMapping("/web/goods")
public class GoodsWebController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserService userService;

    /**
     * 商品列表（公开，默认只查在售商品）
     */
    @PostMapping("/page")
    public AjaxResult page(@RequestBody GoodsQuery query) {
        if (query.getStatus() == null) {
            query.setStatus(Goods.STATUS_ON);
        }
        return AjaxResult.success(goodsService.page(query));
    }

    /**
     * 商品详情（公开）
     */
    @GetMapping("/getInfo/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return AjaxResult.error("商品不存在");
        }
        return AjaxResult.success(goods);
    }

    /**
     * 发布商品
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody Goods goods, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        if (!StringUtils.hasText(goods.getTitle())) {
            return AjaxResult.error("商品标题不能为空");
        }
        if (goods.getPrice() == null) {
            return AjaxResult.error("商品价格不能为空");
        }
        goods.setId(null);
        goods.setUserId(user.getId());
        goods.setStatus(Goods.STATUS_ON);
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        return goodsService.save(goods) ? AjaxResult.success("发布成功", true) : AjaxResult.error("发布失败", false);
    }

    /**
     * 编辑自己的商品
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody Goods goods, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        Goods exist = goodsService.getById(goods.getId());
        if (exist == null) {
            return AjaxResult.error("商品不存在");
        }
        if (!user.getId().equals(exist.getUserId())) {
            return AjaxResult.error("只能修改自己发布的商品");
        }
        goods.setUserId(exist.getUserId());
        goods.setUpdateTime(LocalDateTime.now());
        return goodsService.updateById(goods) ? AjaxResult.success("修改成功", true) : AjaxResult.error("修改失败", false);
    }

    /**
     * 我发布的商品
     */
    @PostMapping("/my")
    public AjaxResult my(@RequestBody GoodsQuery query, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        query.setUserId(user.getId());
        return AjaxResult.success(goodsService.page(query));
    }

    /**
     * 上架 / 下架自己的商品（status: 0下架 1在售）
     */
    @PutMapping("/status/{id}")
    public AjaxResult status(@PathVariable("id") Long id, @RequestBody Goods body, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return AjaxResult.error("商品不存在");
        }
        if (!user.getId().equals(goods.getUserId())) {
            return AjaxResult.error("只能操作自己发布的商品");
        }
        goods.setStatus(body.getStatus());
        goods.setUpdateTime(LocalDateTime.now());
        return goodsService.updateById(goods) ? AjaxResult.success("操作成功", true) : AjaxResult.error("操作失败", false);
    }

    /**
     * 删除自己发布的商品（逻辑删除）
     */
    @DeleteMapping("/dele/{id}")
    public AjaxResult dele(@PathVariable("id") Long id, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return AjaxResult.error("商品不存在");
        }
        if (!user.getId().equals(goods.getUserId())) {
            return AjaxResult.error("只能删除自己发布的商品");
        }
        return goodsService.removeById(id) ? AjaxResult.success("删除成功", true) : AjaxResult.error("删除失败", false);
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