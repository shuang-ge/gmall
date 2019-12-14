package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/13
 * \* Time: 11:52
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
public class CategoryVo extends CategoryEntity {

    //在搜索二级分类的同时，将三级分类也封装进去
    //三级分类
    private List<CategoryEntity> subs;


}
