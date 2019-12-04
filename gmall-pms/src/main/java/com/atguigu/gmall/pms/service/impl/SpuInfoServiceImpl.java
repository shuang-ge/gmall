package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SmsServiceFeign;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SupInfoVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SmsServiceFeign smsServiceFeign;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidOrCondition(QueryCondition condition, Long catId) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //cid =0 代表查询全部
        if (catId != 0) {
            wrapper.eq("catalog_id", catId);
        }
        String key = condition.getKey();
        if (StringUtils.isNoneBlank(key)) {
            wrapper.and(entity -> entity.eq("id", key).or().like("spu_name", key));
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public void saveAllAttrs(SupInfoVo spuInfovo) {
        //spu相关的表
        //pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfovo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUodateTime(spuInfoEntity.getCreateTime());
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();//商品spuid
        //pms_spu_info_desc
        List<String> images = spuInfovo.getSpuImages();
        if (!CollectionUtils.isEmpty(images)) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuId);
            spuInfoDescEntity.setDecript(StringUtils.join(images, ","));
            spuInfoDescDao.insert(spuInfoDescEntity);//保存数据
        }
        //pms_product_attr_value
        List<BaseAttrVo> baseAttrs = spuInfovo.getBaseAttrs();
        baseAttrs.forEach(productAttrValueEntity -> {
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueDao.insert(productAttrValueEntity);
        });

        //sku相关的表
        //pms_sku_info
        List<SkuInfoVo> skus = spuInfovo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(skuInfoVo -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(skuInfoVo, skuInfoEntity);

                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 6));
                skuInfoEntity.setBrandId(spuInfovo.getBrandId());
                skuInfoEntity.setCatalogId(spuInfovo.getCatalogId());
                List<String> skuImage = skuInfoVo.getImages();
                if (!CollectionUtils.isEmpty(skuImage)) {
                    skuInfoEntity.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoEntity.getSkuDefaultImg()) ? skuInfoEntity.getSkuDefaultImg() : skuImage.get(0));
                }
                skuInfoDao.insert(skuInfoEntity);//保存数据
                Long skuId = skuInfoEntity.getSkuId();
                //pms_sku_images
                if (!CollectionUtils.isEmpty(skuImage)) {
                    List<SkuImagesEntity> skuImageses = skuImage.stream().map(s -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setImgUrl(s);
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setDefaultImg(StringUtils.equals(s, skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                        return skuImagesEntity;
                    }).collect(Collectors.toList()); //保存数据
                    this.skuImagesService.saveBatch(skuImageses);
                }
                // pms_sku_sale_attr_value
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();

                List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVo.getSaleAttrs();
                if (!CollectionUtils.isEmpty(saleAttrs)) {
                    saleAttrs.forEach(skuSaleAttr -> {
                        skuSaleAttr.setSkuId(skuId);
                    });
                    this.skuSaleAttrValueService.saveBatch(saleAttrs);
                }

                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuInfoVo, skuSaleVo);
                skuSaleVo.setSkuId(skuId);
                smsServiceFeign.saveSale(skuSaleVo);
            });
        }

    }

}