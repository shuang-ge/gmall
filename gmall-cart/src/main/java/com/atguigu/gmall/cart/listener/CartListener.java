package com.atguigu.gmall.cart.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 11:40
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
public class CartListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient gmallPmsClient;
    private String PRICE_PREFIX = "cart:price";

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "Cart-Queue-Update"
            , durable = "true"), exchange = @Exchange(value = "GMALL-PMS-EXCHANGE"
            , durable = "true", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC), key = "cart.update"))
    public void listen(Long spuId) {
        Resp<List<SkuInfoEntity>> skuInfoResp = this.gmallPmsClient.querySkuInfo(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuInfoResp.getData();
        //根据spuId查询到所有的skuId
        skuInfoEntities.forEach(skuInfoEntity -> {
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuInfoEntity.getSkuId(), skuInfoEntity.getPrice().toString());
        });
    }
}
