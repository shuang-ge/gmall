package com.atguigu.gmall.search.serivce.impl;

import com.atguigu.gmall.search.config.ElasticSearchConfig;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseAttrVO;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import com.atguigu.gmall.search.serivce.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/10
 * \* Time: 20:36
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) throws Exception {

        //构建dsl语句
        SearchRequest searchRequest = this.builderQueryDsl(searchParamVo);

        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchResponseVo responseVo = this.parseSearchResult(response);

        responseVo.setPageNum(searchParamVo.getPageNum());
        responseVo.setPageSize(searchParamVo.getPageSize());
        return responseVo;
    }

    private SearchResponseVo parseSearchResult(SearchResponse response) {

        SearchResponseVo responseVo = new SearchResponseVo();

        SearchHits hits = response.getHits();

        responseVo.setTotal(hits.getTotalHits());

        SearchResponseAttrVO brand = new SearchResponseAttrVO();

        brand.setName("品牌");

        Map<String, Aggregation> stringAggregationMap = response.getAggregations().asMap();

        ParsedLongTerms brandIdAgg = (ParsedLongTerms) stringAggregationMap.get("brandIdAgg");
        List<String> brandValues = brandIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", bucket.getKeyAsString());
            //获取品牌名称，字句和
            Map<String, Aggregation> brandIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandIdSubMap.get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();

            map.put("name", brandName);

            try {
                return objectMapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());

        brand.setValue(brandValues);

        responseVo.setBrand(brand);
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) stringAggregationMap.get("categoryIdAgg");
        List<String> cateValues = categoryIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", bucket.getKeyAsString());
            //获取品牌名称，字句和
            Map<String, Aggregation> categoryIdSubMap = bucket.getAggregations().asMap();

            ParsedStringTerms categoryNameAgg = (ParsedStringTerms) categoryIdSubMap.get("categoryNameAgg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();

            map.put("name", categoryName);

            try {
                return objectMapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());

        SearchResponseAttrVO category = new SearchResponseAttrVO();
        category.setName("分类");
        category.setValue(cateValues);
        responseVo.setCatelog(category);

        SearchHit[] subHit = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        for (SearchHit hit : subHit) {
            String source = hit.getSourceAsString();
            try {
                Goods goods = objectMapper.readValue(source, Goods.class);
                goods.setTitle(hit.getHighlightFields().get("title").getFragments().toString());
                goodsList.add(goods);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        responseVo.setProducts(goodsList);

        //获取嵌套聚合对象
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");

        Map<String, Aggregation> attrAggs = attrAgg.getAggregations().asMap();
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAggs.get("attrIdAgg");

        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<SearchResponseAttrVO> list = buckets.stream().map(bucket -> {
                SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
                attrVO.setProductAttributeId(bucket.getKeyAsNumber().longValue());
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) ((Terms.Bucket) bucket).getAggregations().asMap().get("attrNameAgg");
                attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) ((Terms.Bucket) bucket).getAggregations().asMap().get("attrValueAgg");

                List<? extends Terms.Bucket> valueAggBuckets = attrValueAgg.getBuckets();
                List<String> valueList = valueAggBuckets.stream().map(valuebucket -> {
                    String string = ((Terms.Bucket) valuebucket).getKeyAsString();
                    return string;
                }).collect(Collectors.toList());
                attrVO.setValue(valueList);
                return attrVO;
            }).collect(Collectors.toList());
            responseVo.setAttrs(list);
        }
        return responseVo;
    }


    private SearchRequest builderQueryDsl(SearchParamVo searchParamVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //构建查询条件和，过滤查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String keyword = searchParamVo.getKeyword();//查询关键字
        //构建布尔查询
        if (StringUtils.isEmpty(keyword)) {
            return null;
        }
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //过滤
        String[] brand = searchParamVo.getBrand();
        if (brand != null && brand.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brand));
        }
        String[] catelog3 = searchParamVo.getCatelog3();
        if (catelog3 != null && catelog3.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", catelog3));
        }
        //构建规则属性查询
        String[] props = searchParamVo.getProps();
        if (props != null && props.length != 0) {
            for (String prop : props) {
                String[] splits = StringUtils.split(prop, ":");
                //以冒号进行分割，前面是id, 后面是以—分割的属性
                if (splits.length != 2 || splits == null) {
                    continue;
                }
                String[] attrValues = StringUtils.split(splits[1], "-");
                //构建嵌套查询

                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

                subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", splits[0]));

                subBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));

                boolQueryBuilder.filter(boolQuery);
            }
        }


        //价格区间过滤
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
        Integer priceFrom = searchParamVo.getPriceFrom();
        Integer priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null) {
            rangeQueryBuilder.gte(priceFrom);
        }
        if (priceTo != null) {
            rangeQueryBuilder.lte(priceTo);
        }
        boolQueryBuilder.filter(rangeQueryBuilder);
        ////构建query
        sourceBuilder.query(boolQueryBuilder);
        //构建分页

        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //构建排序
        String order = searchParamVo.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                String field = null;
                switch (split[0]) {
                    case "1":
                        field = "sale";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                sourceBuilder.sort(field, StringUtils.equals("asc", split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }
        //构建高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("title").preTags("<em>").postTags("</em>");
        sourceBuilder.highlighter(highlightBuilder);

        //构建聚合
        //品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId").subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));

        //分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId").subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        //搜索规格属性聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs").subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")).subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));


        System.out.println(sourceBuilder.toString());

        sourceBuilder.fetchSource(new String[]{"skuId", "price", "title", "pic"}, null);


        //查询参数
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(sourceBuilder);


        return searchRequest;
    }


}
