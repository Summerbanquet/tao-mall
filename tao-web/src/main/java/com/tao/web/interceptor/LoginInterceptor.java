package com.tao.web.interceptor;

import com.tao.common.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * 登录拦截器
 * 校验请求头携带的 JWT Token，把当前用户名写入 request 属性
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String USER_NAME_ATTR = "currentUserName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        // 兼容：文件下载走 <a> 直链时无法带自定义请求头，允许通过 query 参数携带 token
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        String userName = TokenUtil.parseToken(token);
        if (userName == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
            return false;
        }
        request.setAttribute(USER_NAME_ATTR, userName);
        return true;
    }
}