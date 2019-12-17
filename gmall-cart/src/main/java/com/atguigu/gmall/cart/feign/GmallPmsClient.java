package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 13:21
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
