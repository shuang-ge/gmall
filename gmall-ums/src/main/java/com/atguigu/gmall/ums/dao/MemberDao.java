package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:54:11
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    void updateBounds(@Param("growth") Integer growth, @Param("integration") Integer integration, @Param("memberId")  Long memberId);
}
