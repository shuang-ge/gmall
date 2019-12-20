package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttr;
import com.atguigu.gmall.search.feign.FeignPmsClient;
import com.atguigu.gmall.search.feign.FeignWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/11
 * \* Time: 16:30
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
public class ReceiveListener {


    @Autowired
    private FeignPmsClient feignPmsClient;
    @Autowired
    private FeignWmsClient feignWmsClient;
    @Autowired
    private GoodsRepository goodsRepository;


    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "Gmall-Save-Queue", durable = "true")
            , exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC)
            , key = {"item.save", "item.insert", "item.update"}))
    public void listen(Long spuId) {

        Resp<List<SkuInfoEntity>> SkuInfoResp = this.feignPmsClient.querySkuInfo(spuId);
        List<SkuInfoEntity> skuInfoEntities = SkuInfoResp.getData();
        if (!CollectionUtils.isEmpty(skuInfoEntities)) {
            List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();
                //设置price
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());

                //设置skuId
                goods.setSkuId(skuInfoEntity.getSkuId());

                //设置createTime
                Resp<SpuInfoEntity> spuInfoEntityResp = this.feignPmsClient.querySpuInfoById(spuId);
                SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                goods.setCreateTime(spuInfoEntity.getCreateTime());

                //设置title
                goods.setTitle(skuInfoEntity.getSkuTitle());

                //设置销量
                goods.setSale(0l);

                //设置pic
                goods.setPic(skuInfoEntity.getSkuDefaultImg());

                Resp<BrandEntity> brandEntityResp = this.feignPmsClient.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brand = brandEntityResp.getData();
                if (brand != null) {
                    //设置brandId
                    goods.setBrandId(brand.getBrandId());
                    //设置brandName
                    goods.setBrandName(brand.getName());
                }
                //查询category
                Resp<CategoryEntity> categoryEntityResp = this.feignPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    //设置categoryId
                    goods.setCategoryId(skuInfoEntity.getCatalogId());
                    //设置categoryName
                    goods.setCategoryName(categoryEntity.getName());
                }
                //查询库存
                Resp<List<WareSkuEntity>> wareSkuEntityResp = this.feignWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = wareSkuEntityResp.getData();
                boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                //设置库存
                goods.setStore(flag);
                //查询规格属性
                Resp<List<ProductAttrValueEntity>> productAttrValueEntitiesResp = this.feignPmsClient.queryProductAttrBySpuId(spuInfoEntity.getId());
                List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueEntitiesResp.getData();
                List<SearchAttr> searches = productAttrValueEntities.stream().map(productAttrValueEntity -> {
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                    searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                    searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                    return searchAttr;
                }).collect(Collectors.toList());
                goods.setAttrs(searches);
                return goods;
            }).collect(Collectors.toList());
            //设置属性参数
            goodsRepository.saveAll(goodsList);
        }
    }
}
