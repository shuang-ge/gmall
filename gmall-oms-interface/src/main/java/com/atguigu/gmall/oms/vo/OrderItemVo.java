package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:03
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String defaultImage;

    private BigDecimal price;

    private BigDecimal corruntPrice;

    private Integer count;

    private List<SkuSaleAttrValueEntity> skuAttrValue;

    private List<SaleVo> saleVos;

    private Boolean store;

    private BigDecimal weight;//重量
}
