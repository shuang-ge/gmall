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


    @Override
    public ItemVo queryItem(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //skuId
        itemVo.setSkuId(skuId);
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
        //spuName
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuInfoById(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
        itemVo.setSpuName(spuInfoEntity.getSpuName());
        //brandEntity
        Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
        BrandEntity brandEntity = brandEntityResp.getData();
        itemVo.setBrandEntity(brandEntity);
        //categoryEntity
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
        CategoryEntity categoryEntity = categoryEntityResp.getData();
        itemVo.setCategoryEntity(categoryEntity);
        //pics
        Resp<List<SkuImagesEntity>> skuImageResp = this.gmallPmsClient.querySkuImagesBySkuId(skuId);
        List<SkuImagesEntity> skuImageList = skuImageResp.getData();
        itemVo.setPics(skuImageList);
        //store  查询库存
        Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
        itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        //Images ,spudesc
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescBySpuId(spuId);
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
        String decript = spuInfoDescEntity.getDecript();
        if (decript != null) {
            String[] split = StringUtils.split(decript, ",");
            itemVo.setImages(Arrays.asList(split));
        }
        //sales
        Resp<List<SaleVo>> saleVoListResp = this.gmallSmsClient.queryBoundsAndAdderBySkuId(skuId);
        List<SaleVo> saleVoList = saleVoListResp.getData();
        itemVo.setSales(saleVoList);

        //saleAttrs  ，根据spuId 查询销售属性
        Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueResp = this.gmallPmsClient.querySkuSaleAttrBySpuId(spuId);
        List<SkuSaleAttrValueEntity> skuSaleAttrValueList = skuSaleAttrValueResp.getData();
        itemVo.setSaleAttrs(skuSaleAttrValueList);

        //groupVos
        Resp<List<ItemGroupVo>> itemGroupVoResp = this.gmallPmsClient.queryItemGroupVoByCatIdAndSpuId(skuInfoEntity.getCatalogId(), spuId);
        List<ItemGroupVo> itemGroupVos = itemGroupVoResp.getData();
        itemVo.setGroupVos(itemGroupVos);
        return itemVo;
    }
}
