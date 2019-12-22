package com.atguigu.gmall.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/13
 * \* Time: 18:10
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient getRedissonClient() {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.80.166:6379");
        return Redisson.create(config);
    }
}
