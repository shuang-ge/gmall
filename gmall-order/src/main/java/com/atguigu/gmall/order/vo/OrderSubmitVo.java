package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 20:52
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class OrderSubmitVo {

    private String token;

    private MemberReceiveAddressEntity addressEntity;

    private List<OrderItemVo> itemVos;

    private Integer bounds;

    private String deliveryCompany; // 配送方式

    private BigDecimal totalPrice;//校验价格

    private Integer payType;

}
