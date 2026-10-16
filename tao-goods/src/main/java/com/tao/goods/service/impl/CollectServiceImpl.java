package com.tao.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.goods.entity.Collect;
import com.tao.goods.mapper.CollectMapper;
import com.tao.goods.service.CollectService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品收藏服务实现
 * 说明：Collect 的 deleted 字段标注了 @TableLogic，删除操作会自动转为 update deleted=1
 */
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Override
    public boolean collect(Long userId, Long goodsId) {
        if (isCollected(userId, goodsId)) {
            return true;
        }
        Collect collect = new Collect();
        collect.setUserId(userId);
        collect.setGoodsId(goodsId);
        collect.setCreateTime(LocalDateTime.now());
        collect.setUpdateTime(LocalDateTime.now());
        return baseMapper.insert(collect) > 0;
    }

    @Override
    public boolean cancel(Long userId, Long goodsId) {
        return baseMapper.delete(new LambdaQueryWrapper<Collect>()
                .eq(Collect::getUserId, userId)
                .eq(Collect::getGoodsId, goodsId)) > 0;
    }

    @Override
    public List<Long> myGoodsIds(Long userId) {
        List<Collect> list = baseMapper.selectList(new LambdaQueryWrapper<Collect>()
                .eq(Collect::getUserId, userId)
                .orderByDesc(Collect::getCreateTime));
        return list.stream().map(Collect::getGoodsId).collect(Collectors.toList());
    }

    @Override
    public boolean isCollected(Long userId, Long goodsId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Collect>()
                .eq(Collect::getUserId, userId)
                .eq(Collect::getGoodsId, goodsId)) > 0;
    }
}