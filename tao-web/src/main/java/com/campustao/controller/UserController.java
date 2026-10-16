package com.campustao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustao.common.result.R;
import com.campustao.user.entity.User;
import com.campustao.user.service.UserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 2.3 新增用户 POST /api/users */
    @PostMapping
    public R<Boolean> add(@RequestBody User user) {
        //password非空约束：不传时给默认密码（真实项目应加密，这里保持课程简单）
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword("123456");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        return R.ok(userService.save(user));
    }

    /** 2.1+2.2 分页+条件查询 GET /api/users/page?pageNum=1&pageSize=10&username=xx&status=1 */
    @GetMapping("/page")
    public R<Page<User>> page(@RequestParam(defaultValue = "1") long pageNum,
                              @RequestParam(defaultValue = "10") long pageSize,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) String nickname,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(username), User::getUsername, username)
                .like(StringUtils.hasText(nickname), User::getNickname, nickname)
                .like(StringUtils.hasText(phone), User::getPhone, phone)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);
        return R.ok(userService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    /** 2.4 编辑用户 PUT /api/users */
    @PutMapping
    public R<Boolean> edit(@RequestBody User user) {
        return R.ok(userService.updateById(user));
    }

    /** 2.5 删除用户 DELETE /api/users/123（配置了逻辑删除，实际执行UPDATE deleted=1） */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(userService.removeById(id));
    }

    /** 用户列表 GET /api/users（保留原有） */
    @GetMapping
    public R<List<User>> list() {
        return R.ok(userService.list());
    }

    /** 按 id 查 GET /api/users/123（保留原有） */
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }
}