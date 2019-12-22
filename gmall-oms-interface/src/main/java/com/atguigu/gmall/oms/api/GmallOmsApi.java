package com.atguigu.gmall.oms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:10
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface GmallOmsApi {

    @PostMapping("oms/order")
    public Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVo orderSubmitVo);


    @PostMapping("oms/order/query/{orderToken}")
    public Resp<OrderEntity> queryOrderByToken(String orderToken);
}
