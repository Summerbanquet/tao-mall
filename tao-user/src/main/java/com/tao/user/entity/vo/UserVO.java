package com.tao.user.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息展示视图类
 */
@Data
public class UserVO implements Serializable {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userName;
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
