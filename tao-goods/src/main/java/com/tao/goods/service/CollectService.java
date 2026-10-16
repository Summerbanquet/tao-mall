package com.tao.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.goods.entity.Collect;

import java.util.List;

/**
 * 商品收藏服务接口
 */
public interface CollectService extends IService<Collect> {

    /**
     * 收藏商品（已收藏则直接返回 true）
     */
    boolean collect(Long userId, Long goodsId);

    /**
     * 取消收藏（逻辑删除）
     */
    boolean cancel(Long userId, Long goodsId);

    /**
     * 查询我收藏的商品ID列表（按收藏时间倒序）
     */
    List<Long> myGoodsIds(Long userId);

    /**
     * 是否已收藏
     */
    boolean isCollected(Long userId, Long goodsId);
}