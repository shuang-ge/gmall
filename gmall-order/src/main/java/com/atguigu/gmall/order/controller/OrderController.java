package com.atguigu.gmall.order.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:22
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/confirm")
    public Resp<OrderConfirmVo> confirm() {
        OrderConfirmVo orderConfirmVo = this.orderService.confirm();
        return Resp.ok(orderConfirmVo);
    }

    @PostMapping("/submit")
    public Resp<Object> submitOrder(@RequestBody OrderSubmitVo orderSubmitVo) {
        this.orderService.submitOrder(orderSubmitVo);

        return Resp.ok(null);
    }
}
