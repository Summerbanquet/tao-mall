package com.tao.web.controller.web;

import com.tao.common.entity.AjaxResult;
import com.tao.user.entity.dto.RegistDTO;
import com.tao.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册 控制器
 */
@RestController
@RequestMapping("/register")
public class RegistWebController {

    @Autowired
    private UserService userService;

    /**
     * 普通用户注册
     * @param registDTO
     * @return
     */
    @PostMapping("regist")
    public AjaxResult regist(@RequestBody RegistDTO registDTO){
        return userService.regist(registDTO) ? AjaxResult.success("注册成功", true) : AjaxResult.error("注册失败", false);
    }
}
