package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroup(QueryCondition queryCondition, Long catId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (catId != null) {
            wrapper.eq("catelog_id", catId);
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public GroupVo queryGroupwithAttrsById(Long gid) {
        GroupVo groupVo = new GroupVo();
        //通过gid查询group
        AttrGroupEntity groupEntity = getById(gid);
        BeanUtils.copyProperties(groupEntity, groupVo);
        //通过gid查询关联关系，并可以获取attr_id
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        if (CollectionUtils.isEmpty(list)) {
            return groupVo;
        }
        groupVo.setRelations(list);
        List<Long> attrIds = list.stream().map(relationEntity -> relationEntity.getAttrId()).collect(Collectors.toList());

        //通过attr_id查询attr
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
        groupVo.setAttrEntities(attrEntities);
        return groupVo;
    }

    @Override
    public List<GroupVo> queryAllAttrs(Long cid) {
        //根据cid 查询组信息
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", cid);
        List<AttrGroupEntity> GroupList = this.list(wrapper);
        //根据查询到的组信息查询所有属性
        List<GroupVo> collect = GroupList.stream().map(groupEntity -> this.queryGroupwithAttrsById(groupEntity.getAttrGroupId())).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<ItemGroupVo> queryItemGroupVoByCatIdAndSpuId(Long cid, Long spuId) {
        //根据分类id查询组id
        List<AttrGroupEntity> attrList = this.attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        return attrList.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupName(attrGroupEntity.getAttrGroupName());
            //根据组id查询 attrId
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupEntity.getAttrGroupId()));
            List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(entity -> {
                return entity.getAttrId();
            }).collect(Collectors.toList());
            //根据attrId 和spuId 查询属性和值
            List<ProductAttrValueEntity> productAttrValueEntities = this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
            itemGroupVo.setBaseAttrs(productAttrValueEntities);
            return itemGroupVo;
        }).collect(Collectors.toList());
    }

}