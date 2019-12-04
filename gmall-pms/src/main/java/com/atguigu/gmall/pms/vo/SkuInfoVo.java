package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/4
 * \* Time: 18:07
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class SkuInfoVo extends SkuInfoEntity {

    private Long skuId;

    private Integer fullCount;

    private BigDecimal discount;

    private Integer ladderAddOther;

    private BigDecimal growBounds;

    private BigDecimal buyBounds;

    private List<Integer> work;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer fullAddOther;

    private List<String> images;

    private List<SkuSaleAttrValueEntity> saleAttrs;
}
