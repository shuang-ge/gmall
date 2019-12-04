package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:27:57
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroup(QueryCondition queryCondition, Long catId);

    GroupVo queryGroupwithAttrsById(Long gid);

    List<GroupVo> queryAllAttrs(Long cid);

}

