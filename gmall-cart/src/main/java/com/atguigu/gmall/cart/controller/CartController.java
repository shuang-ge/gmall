package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 13:12
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    //购物车商品的添加
    @PostMapping("/add")
    public Resp<Object> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);

        return Resp.ok(null);
    }

    @GetMapping("query")
    public Resp<List<Cart>> selectCart() {
        List<Cart> carts = this.cartService.queryCart();
        return Resp.ok(carts);
    }

    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart) {
        this.cartService.updateCart(cart);
        return Resp.ok(null);
    }

    @PostMapping("/delete")
    public Resp<Object> deleteCart(@RequestParam("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return Resp.ok(null);
    }

}
