package com.tao.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tao.goods.entity.Collect;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品收藏数据访问接口
 */
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
}