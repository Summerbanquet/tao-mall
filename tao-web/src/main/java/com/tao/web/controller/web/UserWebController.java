package com.tao.web.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.common.entity.AjaxResult;
import com.tao.common.util.EncryptUtil;
import com.tao.common.util.NickNameUtil;
import com.tao.common.util.TokenUtil;
import com.tao.user.entity.User;
import com.tao.user.entity.dto.LoginDto;
import com.tao.user.entity.dto.RegistDTO;
import com.tao.user.service.UserService;
import com.tao.web.exception.BusinessException;
import com.tao.web.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

/**
 * 用户前端控制器：登录、注册、获取与修改个人信息
 */
@RestController
@RequestMapping("/web/user")
public class UserWebController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginDto dto) {
        if (dto.getUserName() == null || dto.getPassword() == null) {
            return AjaxResult.error("用户名和密码不能为空");
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, dto.getUserName()));
        if (user == null) {
            return AjaxResult.error("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return AjaxResult.error("账号已被禁用，请联系管理员");
        }
        if (!EncryptUtil.matches(dto.getPassword(), user.getPassword())) {
            return AjaxResult.error("用户名或密码错误");
        }
        String token = TokenUtil.createToken(user.getUserName());
        // 脱敏：不要把密码（即使是加密后的）返回给前端
        user.setPassword(null);
        return AjaxResult.success("登录成功").put("token", token).put("userInfo", user);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegistDTO dto) {
        // 1. 必填校验
        if (dto.getUserName() == null || dto.getUserName().trim().isEmpty()) {
            return AjaxResult.error("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return AjaxResult.error("密码不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return AjaxResult.error("姓名不能为空");
        }
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            return AjaxResult.error("手机号不能为空");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return AjaxResult.error("邮箱不能为空");
        }
        // 2. 格式校验
        if (!dto.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return AjaxResult.error("邮箱格式不正确");
        }
        if (!dto.getPhone().matches("^1[3-9]\\d{9}$")) {
            return AjaxResult.error("手机号格式不正确");
        }
        // 3. 唯一性校验
        if (!userService.checkUserNameUnique(dto.getUserName())) {
            return AjaxResult.error("用户名已被占用");
        }
        if (!userService.checkEmailUnique(dto.getEmail())) {
            return AjaxResult.error("该邮箱已被注册");
        }
        // 4. 验证码（演示阶段仅判空）
        if (dto.getValidateCode() == null || dto.getValidateCode().isEmpty()) {
            return AjaxResult.error("请输入验证码");
        }
        // 5. 昵称兜底：未填写时随机生成（如"午后先生7000"）
        String nickName = dto.getNickName();
        if (nickName == null || nickName.trim().isEmpty()) {
            nickName = NickNameUtil.generate();
        }
        // 6. 入库
        User user = new User();
        user.setUserName(dto.getUserName().trim());
        user.setPassword(EncryptUtil.encrypt(dto.getPassword()));
        user.setName(dto.getName().trim());
        user.setNickName(nickName.trim());
        user.setPhone(dto.getPhone().trim());
        user.setEmail(dto.getEmail().trim());
        user.setType(2); // 2-普通用户
        user.setStatus(1); // 1-正常
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        boolean ok = userService.save(user);
        return ok ? AjaxResult.success("注册成功", true) : AjaxResult.error("注册失败", false);
    }

    /**
     * 校验用户名是否唯一（注册前调用）
     */
    @GetMapping("/checkUserName/{userName}")
    public AjaxResult checkUserName(@PathVariable("userName") String userName) {
        boolean unique = userService.checkUserNameUnique(userName);
        return unique ? AjaxResult.success() : AjaxResult.error("用户名已被占用");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public AjaxResult info(HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        if (userName == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return AjaxResult.success(user);
    }

    /**
     * 修改当前登录用户信息
     * 仅更新本次传了值的字段（传 null 表示不修改该项）
     */
    @PutMapping("/info")
    public AjaxResult updateInfo(@RequestBody User form, HttpServletRequest request) {
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        if (userName == null) {
            throw new BusinessException(401, "未登录");
        }
        User current = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
        if (current == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 手机号：非空则校验格式
        if (form.getPhone() != null && !form.getPhone().trim().isEmpty()) {
            if (!form.getPhone().trim().matches("^1[3-9]\\d{9}$")) {
                return AjaxResult.error("手机号格式不正确");
            }
            current.setPhone(form.getPhone().trim());
        }
        // 邮箱：非空则校验格式 + 查重（排除自己）
        if (form.getEmail() != null && !form.getEmail().trim().isEmpty()) {
            String email = form.getEmail().trim();
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return AjaxResult.error("邮箱格式不正确");
            }
            User exist = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
            if (exist != null && !exist.getId().equals(current.getId())) {
                return AjaxResult.error("该邮箱已被其他账号使用");
            }
            current.setEmail(email);
        }
        // 姓名、昵称、头像：非空即更新
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            current.setName(form.getName().trim());
        }
        if (form.getNickName() != null && !form.getNickName().trim().isEmpty()) {
            current.setNickName(form.getNickName().trim());
        }
        if (form.getAvatar() != null && !form.getAvatar().trim().isEmpty()) {
            current.setAvatar(form.getAvatar().trim());
        }

        current.setUpdateTime(LocalDateTime.now());
        boolean ok = userService.updateById(current);
        return ok ? AjaxResult.success("保存成功", true) : AjaxResult.error("保存失败", false);
    }

    /**
     * 用户退出
     */
    @PostMapping("/logout")
    public AjaxResult logout() {
        // 前端清除 token 即可，此处仅返回成功
        return AjaxResult.success("退出成功");
    }
}