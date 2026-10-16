package com.tao.web.controller.adm;

import com.tao.common.entity.AjaxResult;
import com.tao.goods.entity.Goods;
import com.tao.goods.entity.query.GoodsQuery;
import com.tao.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理端商品接口（/adm/** 已在 WebConfig 中配置需要登录）
 */
@RestController
@RequestMapping("/adm/goods")
public class GoodsAdmController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 商品分页条件查询（不限制状态，可查在售与下架）
     */
    @PostMapping("/page")
    public AjaxResult page(@RequestBody GoodsQuery query) {
        return AjaxResult.success(goodsService.page(query));
    }

    /**
     * 商品详情
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
     * 修改商品状态（下架违规商品等）
     */
    @PutMapping("/status/{id}")
    public AjaxResult status(@PathVariable("id") Long id, @RequestBody Goods body) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return AjaxResult.error("商品不存在");
        }
        goods.setStatus(body.getStatus());
        goods.setUpdateTime(LocalDateTime.now());
        return goodsService.updateById(goods) ? AjaxResult.success("操作成功", true) : AjaxResult.error("操作失败", false);
    }

    /**
     * 删除商品（逻辑删除）
     */
    @DeleteMapping("/dele/{id}")
    public AjaxResult dele(@PathVariable("id") Long id) {
        return goodsService.removeById(id) ? AjaxResult.success("删除成功", true) : AjaxResult.error("删除失败", false);
    }
}