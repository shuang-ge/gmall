package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttr;
import com.atguigu.gmall.search.feign.FeignPmsClient;
import com.atguigu.gmall.search.feign.FeignWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {


    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void contextLoads() {
        this.elasticsearchRestTemplate.createIndex(Goods.class);
        this.elasticsearchRestTemplate.putMapping(Goods.class);

    }

    @Autowired
    private FeignPmsClient feignPmsClient;
    @Autowired
    private FeignWmsClient feignWmsClient;
    @Autowired
    private GoodsRepository goodsRepository;


    @Test
    void dataTest() {
        //导入数据
        Long pageSize = 100l;
        Long page = 1l;
        do {
            //分页查询
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setLimit(pageSize);
            queryCondition.setPage(page);

            Resp<List<SpuInfoEntity>> listResp = feignPmsClient.queryPage(queryCondition);
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();
            //数据
            if (!CollectionUtils.isEmpty(spuInfoEntities)) {
                spuInfoEntities.forEach(spuInfoEntity -> {
                    Resp<List<SkuInfoEntity>> SkuInfoResp = this.feignPmsClient.querySkuInfo(spuInfoEntity.getId());
                    List<SkuInfoEntity> skuInfoEntities = SkuInfoResp.getData();
                    if (!CollectionUtils.isEmpty(skuInfoEntities)) {
                        List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                            Goods goods = new Goods();
                            //设置price
                            goods.setPrice(skuInfoEntity.getPrice().doubleValue());

                            //设置skuId
                            goods.setSkuId(skuInfoEntity.getSkuId());

                            //设置createTime
                            goods.setCreateTime(spuInfoEntity.getCreateTime());

                            //设置title
                            goods.setTitle(skuInfoEntity.getSkuTitle());

                            //设置销量
                            goods.setSale(0l);

                            //设置pic
                            goods.setPic(skuInfoEntity.getSkuDefaultImg());

                            Resp<BrandEntity> brandEntityResp = this.feignPmsClient.queryBrandById(skuInfoEntity.getBrandId());
                            BrandEntity brand = brandEntityResp.getData();
                            if (brand != null) {
                                //设置brandId
                                goods.setBrandId(brand.getBrandId());
                                //设置brandName
                                goods.setBrandName(brand.getName());
                            }
                            //查询category
                            Resp<CategoryEntity> categoryEntityResp = this.feignPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                            CategoryEntity categoryEntity = categoryEntityResp.getData();
                            if (categoryEntity != null) {
                                //设置categoryId
                                goods.setCategoryId(skuInfoEntity.getCatalogId());
                                //设置categoryName
                                goods.setCategoryName(categoryEntity.getName());
                            }
                            //查询库存
                            Resp<List<WareSkuEntity>> wareSkuEntityResp = this.feignWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                            List<WareSkuEntity> wareSkuEntities = wareSkuEntityResp.getData();
                            boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                            //设置库存
                            goods.setStore(flag);
                            //查询规格属性
                            Resp<List<ProductAttrValueEntity>> productAttrValueEntitiesResp = this.feignPmsClient.queryProductAttrBySpuId(spuInfoEntity.getId());
                            List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueEntitiesResp.getData();
                            List<SearchAttr> searches = productAttrValueEntities.stream().map(productAttrValueEntity -> {
                                SearchAttr searchAttr = new SearchAttr();
                                searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                                searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                                searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                                return searchAttr;
                            }).collect(Collectors.toList());
                            goods.setAttrs(searches);
                            return goods;
                        }).collect(Collectors.toList());
                        //设置属性参数
                        goodsRepository.saveAll(goodsList);
                    }
                });
            }
            pageSize = (long) spuInfoEntities.size();
            page++;
        } while (pageSize == 100l);
    }
}
