package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
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
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {

}
