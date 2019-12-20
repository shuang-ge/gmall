package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;

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

    void submitOrder(OrderSubmitVo orderSubmitVo);
}
