package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/4
 * \* Time: 19:59
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class SkuSaleVo {

    private Long skuId;

    private BigDecimal growBounds;

    private BigDecimal buyBounds;

    private List<Integer> work;

    private Integer fullCount;

    private BigDecimal discount;

    private Integer ladderAddOther;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer fullAddOther;
}
