package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/3
 * \* Time: 23:01
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
@ToString
public class GroupVo extends AttrGroupEntity {
    private List<AttrEntity> attrEntities;

    private List<AttrAttrgroupRelationEntity> relations;
}
