package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 16:06
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Service
public class CartServiceImpl implements CartService {

    private static final String KEY_PREFIX = "gmall:cart:";
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;


    @Override
    public void addCart(Cart cart) {
        //优先获取购物车中商品的数量
        Integer count = cart.getCount();
        //获取登陆状态
        String key = getLoginStatus();
        //通过key获取redis中的map集合
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //判断购物车中是否有此商品
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            //有,则累加 //通过skuid获取对应的value
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            //反序列化
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count + cart.getCount());
        } else {
            //无，则新增数据
            cart.setCheck(true);
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuIdBySkuId(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());
            Resp<List<SaleVo>> saleVoResp = this.smsClient.queryBoundsAndAdderBySkuId(cart.getSkuId());
            List<SaleVo> saleVos = saleVoResp.getData();
            cart.setSaleVos(saleVos);
            Resp<List<SkuSaleAttrValueEntity>> skuEntitiesResp = this.pmsClient.querySkuSaleAttrBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuEntitiesResp.getData();
            cart.setSkuAttrValue(skuSaleAttrValueEntities);
            Resp<List<WareSkuEntity>> wareSkuResp = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntityList = wareSkuResp.getData();
            cart.setStore(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }
        //重新放入redis
        hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
    }

    @Override
    public List<Cart> queryCart() {
        //获取登陆状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX;
        //未登录状态，只展示未登录购物车
        String unLoginKey = key + userInfo.getUserKey();
        //获取map集合
        BoundHashOperations<String, Object, Object> unboundHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        //获取所有的值（购物车）
        List<Object> cartValues = unboundHashOps.values();
        List<Cart> unLoginCarts = cartValues.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).collect(Collectors.toList());

        if (userInfo.getId() != null) {
            //登陆状态下，合并未登录和登陆购物车
            String loginKey = key + userInfo.getId();
            BoundHashOperations<String, Object, Object> loginBoundHashOps = this.redisTemplate.boundHashOps(loginKey);
            unLoginCarts.forEach(cart -> {
                Integer count = cart.getCount();
                if (loginBoundHashOps.hasKey(cart.getSkuId().toString())) {
                    //有重复商品
                    String cartStr = loginBoundHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartStr, Cart.class);
                    cart.setCount(cart.getCount() + count);//数量相加
                    //再次存入数据库
                }
                loginBoundHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });

            //获取登陆状态下合并后的购物车
            List<Object> loginValues = loginBoundHashOps.values();
            List<Cart> carts = loginValues.stream().map(loginValue -> {
                return JSON.parseObject(loginValue.toString(), Cart.class);
            }).collect(Collectors.toList());
            //删除未登录的购物车
            this.redisTemplate.delete(unLoginKey);
            return carts;
        }
        return unLoginCarts;
    }

    @Override
    public void updateCart(Cart cart) {
        //获取修改后的商品数量
        Integer count = cart.getCount();
        //获取登陆状态
        String key = getLoginStatus();

        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);
        String cartStr = boundHashOps.get(cart.getSkuId().toString()).toString();
        if (StringUtils.isEmpty(cartStr)) {
            return;
        }
        cart = JSON.parseObject(cartStr, Cart.class);
        cart.setCount(count);
        //再次存入数据库
        boundHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
    }

    @Override
    public void deleteCart(Long skuId) {
        //获取登陆状态
        String key = getLoginStatus();
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);
        boundHashOps.delete(skuId.toString());
    }

    private String getLoginStatus() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX;
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }
        return key;
    }
}
