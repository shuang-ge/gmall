package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.entity.Cart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/18
 * \* Time: 18:11
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface GmallCartApi {
    @PostMapping("/cart/{userId}")
    public Resp<List<Cart>> queryCartsByUserId(@PathVariable("userId") Long userId);

}
