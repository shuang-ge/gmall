package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SupInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:27:57
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryByCidOrCondition(QueryCondition condition, Long catId);

    void saveAllAttrs(SupInfoVo spuInfovo);

    void saveSkuInfo(SupInfoVo spuInfovo, Long spuId);

    void saveProAttr(SupInfoVo spuInfovo, Long spuId);

    Long saveSpuInfo(SupInfoVo spuInfovo);
}


