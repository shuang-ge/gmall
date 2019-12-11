package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/10
 * \* Time: 11:18
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@FeignClient("wms-service")
public interface FeignWmsClient extends GmallWmsApi {
}
