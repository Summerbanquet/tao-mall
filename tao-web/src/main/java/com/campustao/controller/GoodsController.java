package com.campustao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustao.common.result.R;
import com.campustao.goods.entity.Goods;
import com.campustao.goods.service.GoodsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /** 发布商品 POST /api/goods */
    @PostMapping
    public R<Boolean> add(@RequestBody Goods goods) {
        return R.ok(goodsService.save(goods));
    }

    /** 分页查商品 GET /api/goods?pageNum=1&pageSize=5 */
    @GetMapping
    public R<Page<Goods>> page(@RequestParam(defaultValue = "1") long pageNum,
                               @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(goodsService.page(new Page<>(pageNum, pageSize)));
    }

    /** 查某用户发布的全部在售商品 GET /api/goods/user/2110255801544339458 */
    @GetMapping("/user/{userId}")
    public R<List<Goods>> listByUser(@PathVariable Long userId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getUserId, userId)
                .eq(Goods::getStatus, 1)
                .orderByDesc(Goods::getCreateTime);
        return R.ok(goodsService.list(wrapper));
    }
}