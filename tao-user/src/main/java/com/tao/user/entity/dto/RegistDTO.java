package com.tao.user.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册数据传输类
 */
@Data
public class RegistDTO implements Serializable {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 真实姓名
     */
    private String name;
    /**
     * 昵称（选填，不填则与姓名相同）
     */
    private String nickName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 短信验证码
     */
    private String validateCode;
}
