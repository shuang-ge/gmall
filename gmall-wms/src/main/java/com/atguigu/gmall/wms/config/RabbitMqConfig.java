package com.atguigu.gmall.wms.config;

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
    @Bean("WMS-TTl-Queue")
    public Queue TtlQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "Gmall-Order-Exchange");
        map.put("x-dead-letter-routing-key", "stock.unlock");
        map.put("x-message-ttl", 70000);
        return new Queue("WMS-TTl-Queue", true, false, false, map);
    }

    @Bean("stock-ttl")
    public Binding querybuilder() {
        return new Binding("WMS-TTl-Queue", Binding.DestinationType.QUEUE, "Gmall-Order-Exchange", "stock.ttl", null);
    }

    //@Bean("WMS-Dead-Queue")
    //public Queue dlQueue() {
    //    return new Queue("WMS-Dead-Queue", true, false, false, null);
    //}
    //
    //@Bean("WMS-dead")
    //public Binding dlQueuebuilder() {
    //    return new Binding("WMS-Dead-Queue", Binding.DestinationType.QUEUE, "Gmall-Order-Exchange", "stock.dead", null);
    //}
}

