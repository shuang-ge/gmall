package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.apache.commons.lang3.StringUtils;
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
 * \* Date: 2019/12/20
 * \* Time: 22:40
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
public class WmsListener {

    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    public static final String STOCK_PREFIX = "wms:stock:";

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "STOCK-UNLOCK", durable = "true")
            , exchange = @Exchange(value = "Gmall-Order-Exchange", durable = "true", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC), key = {"stock.unlock"}))
    public void unlockListener(String orderToken) {
        String skuLockVos = this.redisTemplate.opsForValue().get(STOCK_PREFIX + orderToken);
        if (!StringUtils.isEmpty(skuLockVos)) {
            List<SkuLockVo> lockVoList = JSON.parseArray(skuLockVos, SkuLockVo.class);
            lockVoList.forEach(skuLockVo -> {
                this.wareSkuDao.unlockedStore(skuLockVo.getWareId(), skuLockVo.getCount());
            });
            this.redisTemplate.delete(STOCK_PREFIX + orderToken);
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "DECREMENT-STOCK", durable = "true")
            , exchange = @Exchange(value = "Gmall-Order-Exchange", durable = "true", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC), key = {"delete.stock"}))
    public void decrementStock(String orderToken) {
        String skuLockVos = this.redisTemplate.opsForValue().get(STOCK_PREFIX + orderToken);
        if (!StringUtils.isEmpty(skuLockVos)) {
            List<SkuLockVo> lockVoList = JSON.parseArray(skuLockVos, SkuLockVo.class);
            lockVoList.forEach(skuLockVo -> {
                this.wareSkuDao.decrementStore(skuLockVo.getWareId(), skuLockVo.getCount());
            });
            this.redisTemplate.delete(STOCK_PREFIX + orderToken);
        }
    }
}
