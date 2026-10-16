package com.tao.web.controller.adm;

import com.tao.common.entity.AjaxResult;
import com.tao.goods.entity.Category;
import com.tao.goods.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端分类接口（/adm/** 已在 WebConfig 中配置需要登录）
 */
@RestController
@RequestMapping("/adm/category")
public class CategoryAdmController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        List<Category> list = categoryService.listAll();
        return AjaxResult.success(list);
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody Category category) {
        if (!StringUtils.hasText(category.getName())) {
            return AjaxResult.error("分类名称不能为空");
        }
        category.setId(null);
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return categoryService.save(category) ? AjaxResult.success("新增成功", true) : AjaxResult.error("新增失败", false);
    }

    /**
     * 修改分类
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody Category category) {
        if (category.getId() == null) {
            return AjaxResult.error("分类ID不能为空");
        }
        category.setUpdateTime(LocalDateTime.now());
        return categoryService.updateById(category) ? AjaxResult.success("修改成功", true) : AjaxResult.error("修改失败", false);
    }

    /**
     * 删除分类（逻辑删除）
     */
    @DeleteMapping("/dele/{id}")
    public AjaxResult dele(@PathVariable("id") Long id) {
        return categoryService.removeById(id) ? AjaxResult.success("删除成功", true) : AjaxResult.error("删除失败", false);
    }
}