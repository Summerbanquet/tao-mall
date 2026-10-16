package com.tao.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tao.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
