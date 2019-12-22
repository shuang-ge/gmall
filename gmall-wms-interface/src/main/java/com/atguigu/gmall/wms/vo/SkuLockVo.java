package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 21:10
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class SkuLockVo {

    private Long skuId;
    private Integer count;
    private Long wareId;//被锁定商品的wms_ware_sku id
    private Boolean checked; //是否被锁定

    private String token;
}
