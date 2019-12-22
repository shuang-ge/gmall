package com.atguigu.gmall.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 11:27
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Autowired
    private AuthGatewayFilter gatewayFilter;


    @Override
    public GatewayFilter apply(Object config) {
        return gatewayFilter;
    }
}


