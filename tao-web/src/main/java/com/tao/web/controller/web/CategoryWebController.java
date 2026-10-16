package com.tao.web.controller.web;

import com.tao.common.entity.AjaxResult;
import com.tao.goods.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端分类接口（公开，无需登录）
 */
@RestController
@RequestMapping("/web/category")
public class CategoryWebController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类列表（按排序号升序）
     */
    @GetMapping("/list")
    public AjaxResult list() {
        return AjaxResult.success(categoryService.listAll());
    }
}