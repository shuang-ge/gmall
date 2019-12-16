package com.atguigu.gmall.item.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/16
 * \* Time: 11:41
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Configuration
public class GmallThreadPool {
    @Bean
    public ThreadPoolExecutor getThreadPool() {
        return new ThreadPoolExecutor(50, 300, 10, TimeUnit.HOURS, new ArrayBlockingQueue<>(30));
    }
}
