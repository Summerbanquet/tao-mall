package com.campustao.controller;

import com.campustao.common.result.R;
import com.campustao.user.entity.User;
import com.campustao.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 新增用户 POST /api/users */
    @PostMapping
    public R<Boolean> add(@RequestBody User user) {
        return R.ok(userService.save(user));
    }

    /** 用户列表 GET /api/users */
    @GetMapping
    public R<List<User>> list() {
        return R.ok(userService.list());
    }

    /** 按 id 查 GET /api/users/123 */
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }
}