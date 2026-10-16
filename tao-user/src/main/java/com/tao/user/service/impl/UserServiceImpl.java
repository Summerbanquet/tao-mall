package com.tao.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.common.entity.PageResult;
import com.tao.common.util.EncryptUtil;
import com.tao.common.util.NickNameUtil;
import com.tao.user.entity.User;
import com.tao.user.entity.dto.RegistDTO;
import com.tao.user.entity.query.UserQuery;
import com.tao.user.entity.vo.UserVO;
import com.tao.user.mapper.UserMapper;
import com.tao.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务处理实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public boolean regist(RegistDTO dto) {
        // 加密密码
        String encPwd = EncryptUtil.encrypt(dto.getPassword());
        // 昵称兜底：未填写时随机生成（如"午后先生7000"）
        String nickName = dto.getNickName();
        if (nickName == null || nickName.trim().isEmpty()) {
            nickName = NickNameUtil.generate();
        }
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setPassword(encPwd);
        user.setName(dto.getName());
        user.setNickName(nickName);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setType(2); // 2-普通用户
        user.setStatus(1); // 1-正常
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return save(user);
    }

    @Override
    public PageResult<User> page(UserQuery query) {
        Page<User> pageObj = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(query.getType() != null, User::getType, query.getType())
                .like(query.getUserName() != null, User::getUserName, query.getUserName())
                .like(query.getNickName() != null, User::getNickName, query.getNickName())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus());
        Page<User> page = baseMapper.selectPage(pageObj, queryWrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public List<UserVO> list(UserQuery query) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(query.getType() != null, User::getType, query.getType())
                .like(query.getUserName() != null, User::getUserName, query.getUserName())
                .like(query.getNickName() != null, User::getNickName, query.getNickName())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus());

        List<User> userList = baseMapper.selectList(queryWrapper);
        return userList.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean checkUserNameUnique(String userName) {
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
        return user == null; // 没查到 → 用户名唯一
    }

    @Override
    public boolean checkEmailUnique(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // 空不查重
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        return count == null || count == 0;
    }

    @Override
    public int removes(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            baseMapper.deleteById(id);
            count++;
        }
        return count;
    }
}
