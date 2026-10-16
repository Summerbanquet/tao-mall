package com.tao.user.entity.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户数据分页和条件搜索类
 */
@Data
public class UserQuery implements Serializable {
    /**
     * 用户类型（1运营 2用户）
     */
    private Integer type;
    /**
     * 用户名模糊搜索
     */
    private String userName;
    /**
     * 昵称模糊搜索
     */
    private String nickName;
    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 分页页码
     */
    private Integer pageNum;
    /**
     * 分页大小
     */
    private Integer pageSize;

    public String toString() {
        return type + " " + userName + " " + nickName + " " + status + " " + pageNum + " " +  pageSize;
    }
}
