package com.tao.web.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.user.entity.User;
import com.tao.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 *
 * <p>仅作用于 /adm/**，校验当前登录用户的 type 是否为 1（运营/管理员）。
 * 必须在 LoginInterceptor 之后执行（依赖它写入的当前用户名）。</p>
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    /** 用户类型：1-运营（管理员） 2-普通用户 */
    private static final int TYPE_ADMIN = 1;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String userName = (String) request.getAttribute(LoginInterceptor.USER_NAME_ATTR);
        if (userName == null) {
            write(response, 401, "未登录或登录已过期");
            return false;
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
        if (user == null) {
            write(response, 401, "用户不存在");
            return false;
        }
        if (user.getType() == null || user.getType() != TYPE_ADMIN) {
            write(response, 403, "无权限，仅管理员可操作");
            return false;
        }
        // 禁用账号不允许操作管理端
        if (user.getStatus() != null && user.getStatus() == 0) {
            write(response, 403, "账号已被禁用");
            return false;
        }
        return true;
    }

    private void write(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\"}");
    }
}
