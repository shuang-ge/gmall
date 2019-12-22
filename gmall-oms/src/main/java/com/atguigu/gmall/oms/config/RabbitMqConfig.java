package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/20
 * \* Time: 23:38
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Configuration
public class RabbitMqConfig {
    @Bean("Order-TTl-Queue")
    public Queue TtlQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "Gmall-Order-Exchange");
        map.put("x-dead-letter-routing-key", "order.dead");
        map.put("x-message-ttl", 90000);
        return new Queue("Order-TTl-Queue", true, false, false, map);
    }

    @Bean("order-ttl")
    public Binding querybuilder() {
        return new Binding("Order-TTl-Queue", Binding.DestinationType.QUEUE, "Gmall-Order-Exchange", "order.ttl", null);
    }

    @Bean("Order-Dead-Queue")
    public Queue dlQueue() {
        return new Queue("Order-Dead-Queue", true, false, false, null);
    }

    @Bean("ttl-dead")
    public Binding dlQueuebuilder() {
        return new Binding("Order-Dead-Queue", Binding.DestinationType.QUEUE, "Gmall-Order-Exchange", "order.dead", null);
    }
}

