package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/9
 * \* Time: 19:48
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface GmallPmsApi {

    @PostMapping("pms/spuinfo/queryPage")
    public Resp<List<SpuInfoEntity>> queryPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuInfo(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> queryProductAttrBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySpuInfoById(@PathVariable("id") Long id);

    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryByIdOrLevel(@RequestParam(value = "parentCid", required = false) Long parentCid
            , @RequestParam(value = "level", defaultValue = "0") Integer level);

    @GetMapping("pms/category/category23/{pid}")
    public Resp<List<CategoryVo>> querySubCatgoryById(@PathVariable("pid") Long pid);

    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> querySkuIdBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySpuDescBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/group/attrs/{cid}/{spuId}")
    public Resp<List<ItemGroupVo>> queryItemGroupVoByCatIdAndSpuId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);

    @GetMapping("pms/skusaleattrvalue/cart/{skuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrBySkuId(@PathVariable("skuId") Long skuId);
}
