package com.campustao.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campustao.user.entity.User;
import com.campustao.user.mapper.UserMapper;
import com.campustao.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}