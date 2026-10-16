package com.tao.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.goods.entity.Category;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询全部分类（按排序号升序）
     */
    List<Category> listAll();
}