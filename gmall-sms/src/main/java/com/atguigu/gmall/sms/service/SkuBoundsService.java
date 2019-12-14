package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品sku积分设置
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:48:05
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {


    PageVo queryPage(QueryCondition params);

    void saveSale(SkuSaleVo skuSaleVo);

    List<SaleVo> queryBoundsBySkuId(Long skuId);
}

