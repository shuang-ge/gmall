package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.CacheAnnotation;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/13
 * \* Time: 12:40
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public List<CategoryEntity> queryCatgory1() {
        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryByIdOrLevel(0L, null);
        return listResp.getData();
    }

    @Override
    @CacheAnnotation(prefix = "Cateogories", timeout = 5, random = 5)
    public List<CategoryVo> querySubCategory(Long pid) {
        //优先查询缓存。
        // String subCategorys = this.stringRedisTemplate.opsForValue().get("Cateogories" + pid);
        //如果缓存中存在，则直接返回
        // if (!StringUtils.isEmpty(subCategorys)) {
        // return JSON.parseArray(subCategorys, CategoryVo.class);
        //  }

        //在查询数据库之前加上分布式锁，只放行一个请求访问数据库，
        // RLock lock = this.redissonClient.getLock("lock" + pid);
        //    lock.lock();
        //优先查询缓存。
        // String subCategorys2 = this.stringRedisTemplate.opsForValue().get("Cateogories" + pid);
        //如果缓存中存在，则直接返回
        //     if(!StringUtils.isEmpty(subCategorys2))

        //  {
        //     lock.unlock();
        //     return JSON.parseArray(subCategorys2, CategoryVo.class);
        // }

        //查询数据库
        Resp<List<CategoryVo>> listResp = this.gmallPmsClient.querySubCatgoryById(pid);
        String categoryVoStr = JSON.toJSONString(listResp.getData());
        //将数据设置到缓存，方便下次获取
        //  this.stringRedisTemplate.opsForValue().set("Cateogories" + pid, categoryVoStr, 15 + new Random().

        // nextInt(5), TimeUnit.SECONDS);

        // lock.unlock();

        return listResp.getData();
    }
}
