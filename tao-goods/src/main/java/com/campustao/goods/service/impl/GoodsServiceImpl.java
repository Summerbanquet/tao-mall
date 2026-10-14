package com.campustao.goods.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campustao.goods.entity.Goods;
import com.campustao.goods.mapper.GoodsMapper;
import com.campustao.goods.service.GoodsService;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
}