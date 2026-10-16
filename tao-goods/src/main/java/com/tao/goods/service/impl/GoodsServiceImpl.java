package com.tao.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.common.entity.PageResult;
import com.tao.goods.entity.Goods;
import com.tao.goods.entity.query.GoodsQuery;
import com.tao.goods.mapper.GoodsMapper;
import com.tao.goods.service.GoodsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品服务实现
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Override
    public PageResult<Goods> page(GoodsQuery query) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(Goods::getTitle, query.getTitle());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Goods::getStatus, query.getStatus());
        }
        if (query.getUserId() != null) {
            wrapper.eq(Goods::getUserId, query.getUserId());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, query.getCategoryId());
        }
        wrapper.orderByDesc(Goods::getCreateTime);

        Integer pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        Integer pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        Page<Goods> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }
}