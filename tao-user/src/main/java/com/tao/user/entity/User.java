package com.tao.user.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表 映射实体类
 */
@Data
@TableName("t_user")
public class User implements Serializable {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userName;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 用户类型(1运营 2用户)
     */
    private Integer type;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 手机号码
     */
    private String phone;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 数据状态(0禁用 1正常)
     */
    private Integer status;
    /**
     * 创建时间
     */
    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
