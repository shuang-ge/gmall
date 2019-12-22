package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
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
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {

}
