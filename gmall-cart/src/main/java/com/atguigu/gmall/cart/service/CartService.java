package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 16:06
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface CartService {

    void addCart(Cart cart);

    List<Cart> queryCart();

    void updateCart(Cart cart);

    void deleteCart(Long skuId);
}
