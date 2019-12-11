package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.entity.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/10
 * \* Time: 11:54
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {

}
