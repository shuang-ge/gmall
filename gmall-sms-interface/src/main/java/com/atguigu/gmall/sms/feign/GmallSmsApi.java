package com.atguigu.gmall.sms.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/4
 * \* Time: 22:36
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sku/save")
    public Resp<Object> saveSale(@RequestBody SkuSaleVo skuSaleVo);

    @GetMapping("sms/skubounds/sales/{skuId}")
    public Resp<List<SaleVo>> queryBoundsAndAdderBySkuId(@PathVariable("skuId") Long skuId);
}
