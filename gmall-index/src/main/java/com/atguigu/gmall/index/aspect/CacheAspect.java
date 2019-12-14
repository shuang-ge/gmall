package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.CacheAnnotation;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 11:25
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Aspect
@Component
public class CacheAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    @Around("@annotation(com.atguigu.gmall.index.annotation.CacheAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        //申明一个结果
        Object result = null;

        //获取方法参数对象
        Object[] args = joinPoint.getArgs();
        //获取签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //创建method对象
        Method method = signature.getMethod();
        CacheAnnotation annotation = method.getAnnotation(CacheAnnotation.class);
        //获取注解属性
        String prefix = annotation.prefix();
        int random = annotation.random();
        int timeout = annotation.timeout();


        //获取方法返回值
        Class returnType = signature.getReturnType();
        //查询缓存中是否存在需要查询的信息
        String key = prefix + Arrays.asList(args);

        result = this.cachaHit(key, returnType);
        //加上分布式锁
        RLock lock = this.redissonClient.getLock("lock" + Arrays.asList(args));
        lock.lock();
        //再次判断能否查询到数据
        result = this.cachaHit(key, returnType);

        if (result != null) {
            lock.unlock();
            return result;
        }

        result = joinPoint.proceed(args);

        //将查询到的数据存进缓存中
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout + random, TimeUnit.DAYS);

        lock.unlock();
        return result;
    }

    public Object cachaHit(String key, Class returnType) {

        String result = stringRedisTemplate.opsForValue().get(key);
        //判断  ，查询直接返回
        if (!StringUtils.isEmpty(result)) {
            return JSON.parseObject(result, returnType);
        }
        return null;
    }

}
