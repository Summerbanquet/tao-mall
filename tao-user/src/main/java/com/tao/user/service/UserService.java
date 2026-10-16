package com.tao.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.common.entity.PageResult;
import com.tao.user.entity.User;
import com.tao.user.entity.dto.RegistDTO;
import com.tao.user.entity.query.UserQuery;
import com.tao.user.entity.vo.UserVO;

import java.util.List;

/**
 * 用户业务处理接口
 */
public interface UserService extends IService<User> {
    /**
     * 分页条件查询
     * @param query 查询条件
     * @return 用户列表
     */
    PageResult<User> page(UserQuery query);

    @Override
    default List<User> list() {
        return IService.super.list();
    }

    /** 校验用户名是否可用 */
    boolean checkUserNameUnique(String userName);

    /** 校验邮箱是否已被使用 */
    boolean checkEmailUnique(String email);

    boolean regist(RegistDTO registDTO);

    /**
     * 根据条件获取用户列表
     * @param query 查询条件
     * @return 用户列表
     */
    List<UserVO> list(UserQuery query);
    int removes(Long[] ids);
}
