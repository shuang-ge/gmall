package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/13
 * \* Time: 11:53
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("/cates")
    public Resp<List<CategoryEntity>> queryCatgory1() {
        List<CategoryEntity> categoryEntities = indexService.queryCatgory1();
        return Resp.ok(categoryEntities);
    }

    @GetMapping("/cates/{pid}")
    public Resp<List<CategoryVo>> querySubCategory(@PathVariable("pid") Long pid) {
        List<CategoryVo> categoryVos=this.indexService.querySubCategory(pid);
        return Resp.ok(categoryVos);
    }
}
