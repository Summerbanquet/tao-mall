package com.tao.user.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录数据传输类
 */
@Data
public class LoginDto implements Serializable {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
}
