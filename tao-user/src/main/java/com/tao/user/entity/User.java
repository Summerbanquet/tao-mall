package com.tao.user.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号，全局唯一
     */
    private String username;
    /**
     * BCrypt 加密密文，禁止明文
     */
    private String password;
    /**
     * 昵称，默认取账号
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 学号，实名认证后写入，全局唯一
     */
    private String studentNo;

    /**
     * 学院
     */
    private String college;

    /**
     * 入学年份
     */
    private Integer enrollYear;

    /**
     * 认证状态：0 未认证，1 已认证
     */
    private Integer certStatus;

    /**
     * 信用分，初始 100 分
     */
    private Integer creditScore;

    /**
     * 角色：0 普通学生用户，1 系统管理员
     */
    private Integer role;

    /**
     * 账号状态：0 正常，1 封禁
     */
    private Integer status;

    /**
     * 封禁原因，封禁时展示给用户
     */
    private String banReason;

    /**
     * 个性化推荐开关：1 开启，0 关闭
     */
    private Boolean recommendEnabled;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 注册时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0 正常，1 已注销
     */
    @TableLogic
    private Boolean deleted;
}
