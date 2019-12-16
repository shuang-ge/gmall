package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 13:28
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private ThreadPoolExecutor threadPool;


    @Override
    public ItemVo queryItem(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //skuId
        itemVo.setSkuId(skuId);
        CompletableFuture<SkuInfoEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据skuId查询skuInfo
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuIdBySkuId(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            //skuTitle
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            //subtitle
            itemVo.setSubTitle(skuInfoEntity.getSkuSubtitle());
            //weight
            itemVo.setWeight(skuInfoEntity.getWeight());
            //price
            itemVo.setPrice(skuInfoEntity.getPrice());
            //spuId
            Long spuId = skuInfoEntity.getSpuId();
            itemVo.setSpuId(spuId);
            return skuInfoEntity;
        }, threadPool);

        CompletableFuture<Void> spuNameCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //spuName
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuInfoById(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
            itemVo.setSpuName(spuInfoEntity.getSpuName());
        }, threadPool);
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //brandEntity
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            itemVo.setBrandEntity(brandEntity);
        }, threadPool);
        CompletableFuture<Void> cateCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //categoryEntity
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            itemVo.setCategoryEntity(categoryEntity);
        }, threadPool);
        CompletableFuture<Void> picsCompletableFuture = CompletableFuture.runAsync(() -> {
            //pics
            Resp<List<SkuImagesEntity>> skuImageResp = this.gmallPmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImageList = skuImageResp.getData();
            itemVo.setPics(skuImageList);
        }, threadPool);

        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            //store  查询库存
            Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }, threadPool);
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //Images ,spudesc
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescBySpuId(skuInfoEntity.getSpuId());
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
            String decript = spuInfoDescEntity.getDecript();
            if (decript != null) {
                String[] split = StringUtils.split(decript, ",");
                itemVo.setImages(Arrays.asList(split));
            }
        }, threadPool);
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            //sales
            Resp<List<SaleVo>> saleVoListResp = this.gmallSmsClient.queryBoundsAndAdderBySkuId(skuId);
            List<SaleVo> saleVoList = saleVoListResp.getData();
            itemVo.setSales(saleVoList);
        }, threadPool);
        CompletableFuture<Void> saleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //saleAttrs  ，根据spuId 查询销售属性
            Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueResp = this.gmallPmsClient.querySkuSaleAttrBySpuId(skuInfoEntity.getSpuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueList = skuSaleAttrValueResp.getData();
            itemVo.setSaleAttrs(skuSaleAttrValueList);
        }, threadPool);
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //groupVos
            Resp<List<ItemGroupVo>> itemGroupVoResp = this.gmallPmsClient.queryItemGroupVoByCatIdAndSpuId(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            List<ItemGroupVo> itemGroupVos = itemGroupVoResp.getData();
            itemVo.setGroupVos(itemGroupVos);
        });
        CompletableFuture.allOf(spuNameCompletableFuture, cateCompletableFuture
                , picsCompletableFuture, storeCompletableFuture, descCompletableFuture, salesCompletableFuture
                , saleAttrCompletableFuture, groupCompletableFuture, brandCompletableFuture).join();

        return itemVo;
    }
}
