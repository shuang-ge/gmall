package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVo;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 13:27
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface ItemService {


    ItemVo queryItem(Long skuId);
}
