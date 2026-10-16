package com.tao.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.common.entity.PageResult;
import com.tao.goods.entity.Goods;
import com.tao.goods.entity.query.GoodsQuery;

/**
 * 商品服务接口
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 商品分页条件查询
     */
    PageResult<Goods> page(GoodsQuery query);
}