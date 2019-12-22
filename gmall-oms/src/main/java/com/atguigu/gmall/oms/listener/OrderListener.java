package com.atguigu.gmall.oms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.oms.vo.UserBoundVo;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/20
 * \* Time: 23:53
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
public class OrderListener {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedissonClient redissonClient;

    @RabbitListener(queues = {"Order-Dead-Queue"})
    public void closeListener(String orderToken) {
        //如果关单成功，执行解锁库存
        int i = this.orderDao.closeDao(orderToken);
        if (i == 1) {
            this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "stock.unlock", orderToken);
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "PAY-CART-STATUS-QUEUE"
            , durable = "true")
            , exchange = @Exchange(value = "Gmall-Order-Exchange"
            , durable = "true", ignoreDeclarationExceptions = "true"
            , type = ExchangeTypes.TOPIC)
            , key = {"order.pay"}))
    public void updateCartStatusListener(String orderToken) {
        //修改订单状态
        int i = this.orderDao.minusStock(orderToken);
        if (i == 1) {
            //修改成功 ,,减库存
            this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "delete.stock", orderToken);


            UserBoundVo userBoundVo = new UserBoundVo();
            //查询member
            OrderEntity orderEntity = this.orderDao.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
            userBoundVo.setMemberId(orderEntity.getMemberId());
            userBoundVo.setGrowth(orderEntity.getGrowth());
            userBoundVo.setIntegration(orderEntity.getIntegration());
            this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "add.bounds", JSON.toJSONString(userBoundVo));
        }
    }


    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "Seckill-Order-QUEUE"
            , durable = "true")
            , exchange = @Exchange(value = "Gmall-Order-Exchange"
            , durable = "true", ignoreDeclarationExceptions = "true"
            , type = ExchangeTypes.TOPIC)
            , key = {"order.seckill"}))
    public void CreateSeckillOrderListener(String skuLockVoJson) {
        SkuLockVo skuLockVo = JSON.parseObject(skuLockVoJson, SkuLockVo.class);
        OrderSubmitVo orderSubmitVo = new OrderSubmitVo();
        orderSubmitVo.setToken(skuLockVo.getToken());
        //.......todo
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("count:down:" + skuLockVo.getToken());
        countDownLatch.countDown();
        this.orderService.saveOrder(orderSubmitVo);
    }
}
