package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/4
 * \* Time: 18:03
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class SupInfoVo extends SpuInfoEntity {


    private List<BaseAttrVo> baseAttrs;

    private List<SkuInfoVo> skus;

    private List<String> spuImages;
}
