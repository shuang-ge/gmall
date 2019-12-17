package com.atguigu.gmall.gateway.filter;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 11:26
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilter implements GatewayFilter {
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //获取cookie中的token
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (CollectionUtils.isEmpty(cookies)) {
            //响应状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();  //验证不通过
        }
        //获取cookie
        HttpCookie cookie = cookies.getFirst(this.jwtProperties.getCookieName());
        //判断
        if (StringUtils.isEmpty(cookie)) {
            //响应状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();  //验证不通过
        }
        //校验cookie ,抛出异常则验证不通过
        try {
            JwtUtils.getInfoFromToken(cookie.getValue(), this.jwtProperties.getPublicKey());

        } catch (Exception e) {
            e.printStackTrace();
            //响应状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();  //验证不通过
        }
        return chain.filter(exchange);
    }
}
