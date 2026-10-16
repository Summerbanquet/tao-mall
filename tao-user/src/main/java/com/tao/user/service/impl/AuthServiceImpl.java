package com.tao.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.common.util.EncryptUtil;
import com.tao.common.util.TokenUtil;
import com.tao.user.entity.User;
import com.tao.user.entity.dto.LoginDto;
import com.tao.user.mapper.UserMapper;
import com.tao.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String login(LoginDto loginDto) {
        // 对登录传入的明文密码加密
        String encPwd = EncryptUtil.encrypt(loginDto.getPassword());

        // 查询用户：用户名 + 加密后的密码 同时匹配
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, loginDto.getUserName())
                .eq(User::getPassword, encPwd));

        if(user != null) {
            // 查询到用户，生成token返回
            return TokenUtil.createToken(loginDto.getUserName());
        }
        // 用户不存在/账号密码不匹配，返回null
        return null;
    }
}

