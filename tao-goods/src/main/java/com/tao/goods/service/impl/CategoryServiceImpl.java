package com.tao.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.goods.entity.Category;
import com.tao.goods.mapper.CategoryMapper;
import com.tao.goods.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }
}