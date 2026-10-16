package com.tao.user.service;

import com.tao.user.entity.dto.LoginDto;

public interface AuthService {
    String login(LoginDto loginDto);
}
