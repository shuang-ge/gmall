package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.alipay.AlipayTemplate;
import com.atguigu.gmall.order.alipay.PayAsyncVo;
import com.atguigu.gmall.order.alipay.PayVo;
import com.atguigu.gmall.order.entity.UserInfo;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:22
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @PostMapping("/confirm")
    public Resp<OrderConfirmVo> confirm() {
        OrderConfirmVo orderConfirmVo = this.orderService.confirm();
        return Resp.ok(orderConfirmVo);
    }

    @PostMapping("/submit")
    public Resp<Object> submitOrder(@RequestBody OrderSubmitVo orderSubmitVo) {
        Resp<OrderEntity> orderEntityResp = this.orderService.submitOrder(orderSubmitVo);
        OrderEntity orderEntity = orderEntityResp.getData();
        if (orderEntity != null) {
            try {
                PayVo payVo = new PayVo();
                payVo.setOut_trade_no(orderEntity.getOrderSn());
                payVo.setTotal_amount(orderEntity.getTotalAmount().toString());
                payVo.setSubject("谷粒商城收银台");
                payVo.setBody("在线支付");
                String pay = this.alipayTemplate.pay(payVo);
                System.out.println(pay);
                return Resp.ok(pay);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
        }
        return Resp.ok(null);
    }

    @PostMapping("pay/success")
    public Resp<Object> paysuccess(PayAsyncVo payAsyncVo) {
        String orderToken = payAsyncVo.getOut_trade_no();
        //修改订单状态
        this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "order.pay", orderToken);
        return Resp.ok(null);
    }

    @PostMapping("/miaosha/{skuId}")
    public Resp<Object> seckill(@PathVariable("skuId") Long skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = "seckill:stock:" + skuId;
        if (userInfo.getId() != null) {
            this.redissonClient.getLock("");
            //查询库存是否足够
            String stock = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isEmpty(stock)) {
                return Resp.ok("秒杀结束，欢迎下次再来");
            }
            RSemaphore semaphore = this.redissonClient.getSemaphore("lock:" + skuId);
            semaphore.trySetPermits(500);
            if (semaphore.tryAcquire()) {
                //库存足够，减去库存
                int stockCount = Integer.parseInt(stock);//
                this.redisTemplate.opsForValue().set(key, String.valueOf(--stockCount));

                //创建消息
                SkuLockVo skuLockVo = new SkuLockVo();
                skuLockVo.setSkuId(skuId);
                skuLockVo.setCount(1);
                String idStr = IdWorker.getIdStr();
                skuLockVo.setToken(idStr);
                skuLockVo.setChecked(true);
                this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "order.seckill", JSON.toJSONString(skuLockVo));
                RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("count:down:" + idStr);
                countDownLatch.trySetCount(1);
                semaphore.release();//释放资源
                //响应成功
                return Resp.ok("恭喜，秒杀成功");
            }
            return Resp.ok("再接再厉");
        }
        return Resp.ok(null);
    }

    @GetMapping("query/seckill/{orderToken}")
    public Resp<OrderEntity> querySeckillOrder(String orderToken) throws InterruptedException {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("count:down:" + orderToken);
        countDownLatch.await();//阻塞
        //查询订单
        OrderEntity orderEntity = this.orderService.queryOrder(orderToken);
        return Resp.ok(orderEntity);
    }
}
