package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:00
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class OrderConfirmVo {
    //ums
    private List<MemberReceiveAddressEntity> addressEntities; //详细地址信息
    //cart,pms,sms,
    private List<OrderItemVo> orderItemVos; //购物车确认页信息
    //ums
    private Integer bounds; //积分信息

    private String token; //防止表单重复提交

}

