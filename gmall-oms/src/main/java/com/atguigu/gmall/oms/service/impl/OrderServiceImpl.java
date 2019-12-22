package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.feign.GmallSmsClient;
import com.atguigu.gmall.oms.feign.GmallUmsClient;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallUmsClient gmallUmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private OrderDao orderDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVo orderSubmitVo) {
        List<OrderItemVo> itemVos = orderSubmitVo.getItemVos();
        if (CollectionUtils.isEmpty(itemVos)) {
            throw new OrderException("创建订单失败");
        }
        //创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setAutoConfirmDay(30);
        orderEntity.setBillContent("发票内容");
        orderEntity.setBillHeader("发票抬头");
        orderEntity.setBillReceiverEmail("2511937971@qq.com");
        orderEntity.setBillType(1);
        orderEntity.setBillReceiverPhone("18736362555");
        orderEntity.setCreateTime(new Date());
        orderEntity.setCommentTime(orderEntity.getCreateTime());
        orderEntity.setConfirmStatus(0);
        orderEntity.setCouponAmount(new BigDecimal(orderSubmitVo.getBounds() / 100));
        orderEntity.setCouponId(null);
        orderEntity.setDeleteStatus(0);
        orderEntity.setDeliveryCompany(orderSubmitVo.getDeliveryCompany());
        orderEntity.setDeliverySn(UUID.randomUUID().toString().substring(0, 10));
        orderEntity.setDeliveryTime(null);
        orderEntity.setDiscountAmount(null);
        orderEntity.setFreightAmount(new BigDecimal(10));
        orderEntity.setGrowth(20);
        orderEntity.setOrderSn(orderSubmitVo.getToken());
        orderEntity.setIntegration(20);
        orderEntity.setIntegrationAmount(new BigDecimal(orderSubmitVo.getBounds() / 100));
        orderEntity.setMemberId(orderSubmitVo.getUserId());
        Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryMemberById(orderSubmitVo.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderEntity.setMemberUsername(memberEntity.getUsername());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setNote("要好的");
        orderEntity.setPayAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setPaymentTime(null);
        orderEntity.setPayType(orderSubmitVo.getPayType());
        orderEntity.setPromotionAmount(new BigDecimal(10));
        MemberReceiveAddressEntity addressEntity = orderSubmitVo.getAddressEntity();
        orderEntity.setReceiverCity(addressEntity.getCity());
        orderEntity.setReceiverDetailAddress(addressEntity.getDetailAddress());
        orderEntity.setReceiverName(addressEntity.getName());
        orderEntity.setReceiverPhone(addressEntity.getPhone());
        orderEntity.setReceiverPostCode(addressEntity.getPostCode());
        orderEntity.setReceiverProvince(addressEntity.getProvince());
        orderEntity.setReceiverRegion(addressEntity.getRegion());
        orderEntity.setReceiveTime(null);
        orderEntity.setSourceType(1);
        orderEntity.setStatus(0);
        orderEntity.setTotalAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setUseIntegration(orderSubmitVo.getBounds());
        //保存到数据库
        this.save(orderEntity);
        //创建orderitem详情
        itemVos.stream().forEach(orderItemVo -> {
            OrderItemEntity itemEntity = new OrderItemEntity();
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuIdBySkuId(orderItemVo.getSkuId());
            itemEntity.setSkuId(orderItemVo.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            itemEntity.setSkuName(skuInfoEntity.getSkuName());
            itemEntity.setCategoryId(skuInfoEntity.getCatalogId());
            itemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
            itemEntity.setSkuPrice(skuInfoEntity.getPrice());
            itemEntity.setSkuAttrsVals(JSON.toJSONString(orderItemVo.getSkuAttrValue()));
            itemEntity.setOrderSn(orderEntity.getOrderSn());
            Resp<SkuBoundsEntity> skuBoundsEntityResp = this.gmallSmsClient.queryBoundsBySkuId(orderItemVo.getSkuId());
            SkuBoundsEntity skuBoundsEntity = skuBoundsEntityResp.getData();
            itemEntity.setCouponAmount(skuBoundsEntity.getBuyBounds());
            itemEntity.setGiftGrowth(skuBoundsEntity.getGrowBounds().intValue());
            itemEntity.setGiftIntegration(skuBoundsEntity.getBuyBounds().intValue());
            itemEntity.setSkuQuantity(orderItemVo.getCount());
            itemEntity.setIntegrationAmount(new BigDecimal(10));
            itemEntity.setOrderId(orderEntity.getId());
            itemEntity.setPromotionAmount(new BigDecimal(20));
            itemEntity.setRealAmount(orderItemVo.getPrice().subtract(itemEntity.getPromotionAmount()));
            Long spuId = skuInfoEntity.getSpuId();
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuInfoById(spuId);
            SpuInfoEntity spuInfo = spuInfoEntityResp.getData();
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBoundsById(spuInfo.getBrandId());
            itemEntity.setSpuBrand(brandEntityResp.getData().getName());
            itemEntity.setSpuId(spuId);
            itemEntity.setSpuName(spuInfo.getSpuName());
            itemEntity.setSpuPic(spuInfo.getSpuDescription());
            this.orderItemDao.insert(itemEntity);
        });
        this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "order.ttl", orderSubmitVo.getToken());

        return orderEntity;
    }

    @Override
    public OrderEntity queryOrderByToken(String orderToken) {
        return this.orderDao.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
    }

}