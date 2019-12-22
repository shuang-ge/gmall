package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String STOCK_PREFIX = "wms:stock:";

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<WareSkuEntity> querySkuInfoById(Long skuId) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId);
        List<WareSkuEntity> list = wareSkuDao.selectList(wrapper);
        return list;
    }

    @Override
    @Transactional
    public String ckeckedAndLockSkuWare(List<SkuLockVo> lockVos) {
        if (CollectionUtils.isEmpty(lockVos)) {
            return "没有选中的商品";
        }
        lockVos.forEach(skuLockVo -> lockStore(skuLockVo));
        //过滤出未锁定的商品信息
        List<SkuLockVo> unLockList = lockVos.stream().filter(skuLockVo -> skuLockVo.getChecked() == false).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unLockList)) {
            //代表库存不足
            //回滚锁定
            List<SkuLockVo> lockVoList = lockVos.stream().filter(skuLockVo -> skuLockVo.getChecked() == true).collect(Collectors.toList());
            lockVoList.forEach(skuLockVo -> this.wareSkuDao.unlockedStore(skuLockVo.getWareId(), skuLockVo.getCount()));
            return "商品：" + unLockList.toString() + "库存不足";
        }
        SkuLockVo skuLockVo = lockVos.get(0);
        this.redisTemplate.opsForValue().set(STOCK_PREFIX + skuLockVo.getToken(), JSON.toJSONString(lockVos));
        this.amqpTemplate.convertAndSend("Gmall-Order-Exchange", "stock.ttl", skuLockVo.getToken());
        return null;
    }

    public void lockStore(SkuLockVo skuLockVo) {
        //分布式锁
        RLock lock = this.redissonClient.getLock("sku:lock:" + skuLockVo.getSkuId());
        lock.lock();
        //检查
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkedStore(skuLockVo.getSkuId(), skuLockVo.getCount());
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            //库存不足
            skuLockVo.setChecked(false);
        } else {
            //选择合适的仓库
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            //设置id
            skuLockVo.setWareId(wareSkuEntity.getId());
            skuLockVo.setChecked(true);
            //锁定库存
            this.wareSkuDao.lockedStore(skuLockVo.getWareId(), skuLockVo.getCount());
        }
        lock.unlock();
    }
}