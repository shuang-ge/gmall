package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.vo.OrderConfirmVo;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:25
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface OrderService {
    OrderConfirmVo confirm();

    Resp<OrderEntity> submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity queryOrder(String orderToken);
}
