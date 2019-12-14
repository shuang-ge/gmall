package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品sku积分设置
 * 
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:48:05
 */
@Mapper
public interface SkuBoundsDao extends BaseMapper<SkuBoundsEntity> {

    void selectOne(QueryWrapper<SaleVo> sku_id);
}
