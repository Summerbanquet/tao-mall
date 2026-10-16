package com.tao.web.config;

import com.tao.common.util.TokenUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置：把签名密钥从配置文件注入到 TokenUtil
 *
 * <p>密钥不再硬编码在代码里，避免随源码提交而泄露。
 * 生产环境通过环境变量 JWT_SECRET 注入即可，无需改代码。</p>
 */
@Configuration
public class JwtConfig implements InitializingBean {

    @Value("${tao.jwt.secret}")
    private String secret;

    @Value("${tao.jwt.expire-millis:604800000}")
    private long expireMillis;

    @Override
    public void afterPropertiesSet() {
        TokenUtil.setSecret(secret);
        TokenUtil.setExpireMillis(expireMillis);
    }
}
