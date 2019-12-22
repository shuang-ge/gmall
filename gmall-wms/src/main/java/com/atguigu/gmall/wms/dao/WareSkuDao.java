package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:59:06
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
    List<WareSkuEntity> checkedStore(@Param("skuId") Long skuId, @Param("count") Integer count);

    void lockedStore(@Param("wareId") Long wareId, @Param("count") Integer count);

    void unlockedStore(@Param("wareId") Long wareId, @Param("count") Integer count);

    void decrementStore(@Param("wareId") Long wareId, @Param("count") Integer count);
}
