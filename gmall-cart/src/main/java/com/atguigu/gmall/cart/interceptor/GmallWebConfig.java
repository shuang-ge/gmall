package com.atguigu.gmall.cart.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 12:55
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Configuration
public class GmallWebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }
}
