package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/13
 * \* Time: 12:39
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface IndexService {
    List<CategoryEntity> queryCatgory1();

    List<CategoryVo> querySubCategory(Long pid);
}
