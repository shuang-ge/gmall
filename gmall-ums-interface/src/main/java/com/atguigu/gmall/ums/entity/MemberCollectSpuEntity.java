package com.atguigu.gmall.ums.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员收藏的商品
 * 
 * @author shuangge
 * @email 2511937971@qq.com
 * @date 2019-12-03 18:54:11
 */
@ApiModel
@Data
@TableName("ums_member_collect_spu")
public class MemberCollectSpuEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	@ApiModelProperty(name = "id",value = "id")
	private Long id;
	/**
	 * 会员id
	 */
	@ApiModelProperty(name = "memberId",value = "会员id")
	private Long memberId;
	/**
	 * spu_id
	 */
	@ApiModelProperty(name = "spuId",value = "spu_id")
	private Long spuId;
	/**
	 * spu_name
	 */
	@ApiModelProperty(name = "spuName",value = "spu_name")
	private String spuName;
	/**
	 * spu_img
	 */
	@ApiModelProperty(name = "spuImg",value = "spu_img")
	private String spuImg;
	/**
	 * create_time
	 */
	@ApiModelProperty(name = "createTime",value = "create_time")
	private Date createTime;

}
