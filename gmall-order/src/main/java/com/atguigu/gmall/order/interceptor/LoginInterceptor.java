package com.atguigu.gmall.order.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.order.config.JwtProperties;
import com.atguigu.gmall.order.entity.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 12:43
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();

        //获取tonken信息
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, this.jwtProperties.getUserKey());
        //判断userkey是否为空
        if (userKey == null) {
            //设置userkey
            userKey = UUID.randomUUID().toString();
            //设置到cookie中
            CookieUtils.setCookie(request, response, this.jwtProperties.getUserKey(), userKey, 6 * 30 * 24 * 3600);
        }
        userInfo.setUserKey(userKey);
        //解析token
        if (!StringUtils.isEmpty(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            if (!CollectionUtils.isEmpty(map)) {
                userInfo.setId(new Long(map.get("id").toString()));
            }
        }
        THREAD_LOCAL.set(userInfo);
        return super.preHandle(request, response, handler);
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    //线程结束后自动移除线程内的数据，防止内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }
}
