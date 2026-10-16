package com.tao.web.controller.common;

import com.tao.common.entity.AjaxResult;
import com.tao.user.entity.dto.LoginDto;
import com.tao.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录（支持管理端、用户端）
     * @param loginDto
     * @return
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginDto loginDto) {
        String token = authService.login(loginDto);
        if(StringUtils.hasText(token)) {
            return AjaxResult.success("登录成功", token);
        }
        return AjaxResult.error("登录失败", null);
    }
}
