package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 会员收货地址
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:54:11
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageVo queryPage(QueryCondition params);

    List<MemberReceiveAddressEntity> queryAddressByUserId(Long userId);
}

