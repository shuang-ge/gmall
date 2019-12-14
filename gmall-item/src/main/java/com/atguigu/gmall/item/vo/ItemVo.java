package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 13:03
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class ItemVo {

    private Long skuId;

    private BrandEntity brandEntity;

    private CategoryEntity categoryEntity;

    private String skuTitle;

    private String subTitle;

    private BigDecimal price;

    private BigDecimal weight;

    private Long spuId;

    private List<SkuImagesEntity> pics; //sku图片列表

    private String spuName;

    private List<SaleVo> sales; //营销信息

    private Boolean store;//是否有货

    private List<SkuSaleAttrValueEntity> saleAttrs;//销售属性

    private List<String> images;// spu海报

    private List<ItemGroupVo> groupVos;//规格参数组以及租下的参数

}
