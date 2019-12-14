package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 13:19
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class ItemGroupVo {

    private String groupName;

    private List<ProductAttrValueEntity> baseAttrs;
 }
