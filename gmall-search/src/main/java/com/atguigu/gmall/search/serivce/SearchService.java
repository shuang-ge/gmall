package com.atguigu.gmall.search.serivce;

import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/10
 * \* Time: 20:33
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface SearchService {


    SearchResponseVo search(SearchParamVo searchParamVo) throws Exception;
}
