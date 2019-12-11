package com.atguigu.gmall.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/9
 * \* Time: 19:04
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Data
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods {
    @Id
    private Long skuId;

    @Field(type = FieldType.Keyword, index = false)
    private String pic;

    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    @Field(type = FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Double)
    private Double price;
    @Field(type = FieldType.Long)
    private Long sale;
    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Nested)
    private List<SearchAttr> attrs;

    @Field(type = FieldType.Boolean)
    private Boolean store;

}
