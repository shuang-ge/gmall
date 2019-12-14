package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuBoundsDao skuBoundsDao;

    @Autowired
    private SkuLadderDao skuLadderDao;
    @Autowired
    private SkuFullReductionDao skuFullReductionDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }


    @Override
    @Transactional
    public void saveSale(SkuSaleVo skuSaleVo) {
        //sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setBuyBounds(skuSaleVo.getBuyBounds());
        skuBoundsEntity.setGrowBounds(skuSaleVo.getGrowBounds());
        skuBoundsEntity.setSkuId(skuSaleVo.getSkuId());
        List<Integer> work = skuSaleVo.getWork();
        skuBoundsEntity.setWork(work.get(3) * 1 + work.get(2) * 2 + work.get(1) * 4 + work.get(0) * 8);
        this.save(skuBoundsEntity);
        //sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuSaleVo.getSkuId());
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        skuLadderEntity.setDiscount(skuSaleVo.getDiscount());
        skuLadderEntity.setFullCount(skuSaleVo.getFullCount());
        skuLadderEntity.setPrice(skuSaleVo.getFullPrice());
        skuLadderDao.insert(skuLadderEntity);
        //sms_sku_full_reduce
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();

        skuFullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        skuFullReductionEntity.setFullPrice(skuSaleVo.getFullPrice());
        skuFullReductionEntity.setReducePrice(skuSaleVo.getReducePrice());
        skuFullReductionEntity.setSkuId(skuSaleVo.getSkuId());
        skuFullReductionDao.insert(skuFullReductionEntity);


    }

    @Override
    public List<SaleVo> queryBoundsBySkuId(Long skuId) {
        List<SaleVo> saleVos = new ArrayList<>();
        //积分信息
        SkuBoundsEntity skuBoundsEntity = this.skuBoundsDao.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        SaleVo broundSaleVo = new SaleVo();
        broundSaleVo.setType("积分");
        StringBuffer buffer = new StringBuffer();
        if (skuBoundsEntity != null) {
            if (skuBoundsEntity.getGrowBounds() != null) {
                buffer.append("赠送成长积分：" + skuBoundsEntity.getGrowBounds() + "分");
            }
            if (skuBoundsEntity.getBuyBounds() != null) {
                if (buffer.equals("")) {
                    buffer.append("赠送购物积分：" + skuBoundsEntity.getBuyBounds() + "分");
                } else {
                    buffer.append(",赠送购物积分：" + skuBoundsEntity.getBuyBounds() + "分");
                }
            }
        }
        broundSaleVo.setDesc(buffer.toString());
        saleVos.add(broundSaleVo);
        //满减信息
        SaleVo fullSaleVo = new SaleVo();
        fullSaleVo.setType("满减");
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        buffer = new StringBuffer();
        if (skuFullReductionEntity != null) {
            buffer.append("满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice() + "元");
            fullSaleVo.setDesc(buffer.toString());
        }
        saleVos.add(fullSaleVo);
        //折扣信息
        SaleVo ladderSaleVo = new SaleVo();
        ladderSaleVo.setType("折扣");
        buffer = new StringBuffer();
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuLadderEntity != null) {
            buffer.append("满" + skuLadderEntity.getFullCount() + "件" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            ladderSaleVo.setDesc(buffer.toString());
        }
        saleVos.add(ladderSaleVo);
        return saleVos;
    }

}