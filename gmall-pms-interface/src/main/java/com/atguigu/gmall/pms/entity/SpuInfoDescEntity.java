package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * spu信息介绍
 *
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:27:57
 */
@ApiModel
@Data
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品id
     */
    @TableId(type = IdType.INPUT)
    @ApiModelProperty(name = "spuId", value = "商品id")
    private Long spuId;
    /**
     * 商品介绍
     */
    @ApiModelProperty(name = "decript", value = "商品介绍")
    private String decript;

}
