package com.atguigu.gmall.order.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.entity.UserInfo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:25
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final String KEY_PREFIX = "gmall:cart:";

    private static final String TOKEN_PREFIX = "order:token:";
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallUmsClient gmallUmsClient;

    @Autowired
    private GmallCartClient gmallCartClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private GmallOmsClient gmallOmsClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //获取登陆状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println(userInfo.getId());
        Long id = userInfo.getId();
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据id获取收获地址
            Resp<List<MemberReceiveAddressEntity>> addressListResp = this.gmallUmsClient.queryAddressByUserId(id);
            List<MemberReceiveAddressEntity> addresses = addressListResp.getData();
            //设置收货地址
            confirmVo.setAddressEntities(addresses);
        }, threadPoolExecutor);

        CompletableFuture<Void> IntegrationCompletableFuture = CompletableFuture.runAsync(() -> {
            //设置积分
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryMemberById(id);
            MemberEntity memberEntity = memberEntityResp.getData();
            confirmVo.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);


        CompletableFuture<List<Cart>> cartsCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //设置购物车页
            //获取购物车
            Resp<List<Cart>> cartsListResp = this.gmallCartClient.queryCartsByUserId(id);
            List<Cart> carts = cartsListResp.getData();
            if (CollectionUtils.isEmpty(carts)) {
                throw new OrderException("请选择商品");
            }
            return carts;
        });

        CompletableFuture<Void> orderItemCompletableFuture = cartsCompletableFuture.thenAcceptAsync(carts -> {
            List<OrderItemVo> orderItemVos = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setCount(cart.getCount());
                orderItemVo.setSkuId(cart.getSkuId());
                CompletableFuture<SkuInfoEntity> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuIdBySkuId(cart.getSkuId());
                    SkuInfoEntity skuInfo = skuInfoEntityResp.getData();
                    orderItemVo.setDefaultImage(skuInfo.getSkuDefaultImg());
                    orderItemVo.setCorruntPrice(skuInfo.getPrice());
                    orderItemVo.setPrice(skuInfo.getPrice());
                    orderItemVo.setTitle(skuInfo.getSkuTitle());
                    orderItemVo.setWeight(skuInfo.getWeight());
                    return skuInfo;
                }, threadPoolExecutor);
                CompletableFuture<Void> storeCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                    //wms//设置库存
                    Resp<List<WareSkuEntity>> wareSkulistResp = this.gmallWmsClient.queryWareSkuBySkuId(skuInfo.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareSkulistResp.getData();
                    orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                }, threadPoolExecutor);
                CompletableFuture<Void> saleCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                    //sms销售属性
                    Resp<List<SaleVo>> boundsAndAdderResp = this.gmallSmsClient.queryBoundsAndAdderBySkuId(skuInfo.getSkuId());
                    List<SaleVo> saleVos = boundsAndAdderResp.getData();
                    orderItemVo.setSaleVos(saleVos);
                }, threadPoolExecutor);
                CompletableFuture<Void> saleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                    //设置营销属性
                    Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrResp = this.gmallPmsClient.querySkuSaleAttrBySkuId(skuInfo.getSkuId());
                    List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleAttrResp.getData();
                    orderItemVo.setSkuAttrValue(skuSaleAttrValueEntities);
                }, threadPoolExecutor);
                CompletableFuture.allOf(storeCompletableFuture, saleCompletableFuture, saleAttrCompletableFuture).join();
                return orderItemVo;
            }).collect(Collectors.toList());
            confirmVo.setOrderItemVos(orderItemVos);
        }, threadPoolExecutor);
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            //设置token
            String token = IdWorker.getTimeId();
            confirmVo.setToken(token);
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX + token, token);
        }, threadPoolExecutor);
        CompletableFuture.allOf(tokenCompletableFuture, orderItemCompletableFuture, cartsCompletableFuture, IntegrationCompletableFuture, addressCompletableFuture).join();
        return confirmVo;
    }

    @Override
    public Resp<OrderEntity> submitOrder(OrderSubmitVo orderSubmitVo) {
        //检验token ，防止重复提交
        String token = orderSubmitVo.getToken();
        // String token = this.redisTemplate.opsForValue().get(TOKEN_PREFIX + orderSubmitVo.getToken());
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long num = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderSubmitVo.getToken()), token);
        if (num == 0l) {
            throw new OrderException("订单已提交，请勿重复提交");
        }
        //校验价格是否相同
        //前台提交的总价格
        BigDecimal price = orderSubmitVo.getTotalPrice();
        List<OrderItemVo> itemVos = orderSubmitVo.getItemVos();
        if (CollectionUtils.isEmpty(itemVos)) {
            throw new OrderException("页面已经过期，请刷新重试");
        }
        BigDecimal finalPrice = itemVos.stream().map(orderItemVo -> {
            Long skuId = orderItemVo.getSkuId();
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuIdBySkuId(skuId);
            SkuInfoEntity skuInfo = skuInfoEntityResp.getData();
            if (skuInfo == null) {
                return new BigDecimal(0);
            }
            return skuInfo.getPrice().multiply(new BigDecimal(orderItemVo.getCount()));
        }).reduce((a, b) -> a.add(b)).get();

        if (finalPrice.compareTo(price) != 0) {
            //价格不一致，，直接返回
            throw new OrderException("页面数据已过期，请刷新重新下单");
        }
        //校验库存并锁定库存
        List<SkuLockVo> lockVos = itemVos.stream().map(orderItemVo -> {
            SkuLockVo lockVo = new SkuLockVo();
            lockVo.setSkuId(orderItemVo.getSkuId());
            lockVo.setCount(orderItemVo.getCount());
            lockVo.setToken(token);
            return lockVo;
        }).collect(Collectors.toList());

        Resp<String> lockResp = this.gmallWmsClient.ckeckedAndLockSkuWare(lockVos);
        if (lockResp.getCode() == 1) {
            //代表锁定失败
            throw new OrderException("库存不足" + lockResp.getMsg());
        }
        //下单//创建订单
        UserInfo userInfo = null;
        Resp<OrderEntity> orderEntityResp = null;
        try {
            userInfo = LoginInterceptor.getUserInfo();
            orderSubmitVo.setUserId(userInfo.getId());
            orderEntityResp = this.gmallOmsClient.saveOrder(orderSubmitVo);
        } catch (Exception e) {
            e.printStackTrace();
            //发送消息给库存， 去解锁库存
            this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "stock.unlock", token);
            throw new OrderException("服务器错误，请刷新重时");
        }
        //删除购物车 (消息队列) 获取skuIds
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getId());
        List<String> skuIds = itemVos.stream().map(orderItemVo -> orderItemVo.getSkuId().toString()).collect(Collectors.toList());
        map.put("skuIds", skuIds);
        this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "cart.delete", map);
        if (orderEntityResp != null) {
            return orderEntityResp;
        }
        return null;
    }

    @Override
    public OrderEntity queryOrder(String orderToken) {
        Resp<OrderEntity> orderEntityResp = this.gmallOmsClient.queryOrderByToken(orderToken);
        OrderEntity orderEntity = orderEntityResp.getData();
        return orderEntity;
    }
}
