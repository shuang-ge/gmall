package com.atguigu.gmall.order.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.order.entity.UserInfo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderItemVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    public void submitOrder(OrderSubmitVo orderSubmitVo) {
        //检验token ，防止重复提交
        String token = this.redisTemplate.opsForValue().get(TOKEN_PREFIX + orderSubmitVo.getToken());
        if (StringUtils.isEmpty(token)) {
            //没有，代表不是第一次
            throw new OrderException("请勿重复提交");
        }

        //校验价格是否相同


        //校验库存并锁定库存
        //下单
        //删除购物车
    }
}
