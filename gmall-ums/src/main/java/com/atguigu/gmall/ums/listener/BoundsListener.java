package com.atguigu.gmall.ums.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.oms.vo.UserBoundVo;
import com.atguigu.gmall.ums.dao.MemberDao;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/21
 * \* Time: 19:01
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Component
public class BoundsListener {
    @Autowired
    private MemberDao memberDao;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "PAY-Bounds-ADD-QUEUE"
            , durable = "true")
            , exchange = @Exchange(value = "Gmall-Order-Exchange"
            , durable = "true", ignoreDeclarationExceptions = "true"
            , type = ExchangeTypes.TOPIC)
            , key = {"add.bounds"}))
    public void addBounds(String bounds) {
        UserBoundVo boundVo = JSON.parseObject(bounds, UserBoundVo.class);
        this.memberDao.updateBounds(boundVo.getGrowth(), boundVo.getIntegration(), boundVo.getMemberId());

    }
}
