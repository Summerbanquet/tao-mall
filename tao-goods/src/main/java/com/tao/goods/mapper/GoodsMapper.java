package com.tao.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tao.goods.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问接口
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}