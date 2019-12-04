package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/4
 * \* Time: 18:23
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public class BaseAttrVo extends ProductAttrValueEntity {

    public void setValueSelected(List<String> selected) {
        if (!CollectionUtils.isEmpty(selected)) {
            this.setAttrValue(StringUtils.join(selected, ","));
        }
    }
}
