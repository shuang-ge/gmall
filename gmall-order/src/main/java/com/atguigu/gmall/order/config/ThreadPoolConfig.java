package com.atguigu.gmall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 17:58
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor getThreadPool() {
        return new ThreadPoolExecutor(200, 500, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }
}
