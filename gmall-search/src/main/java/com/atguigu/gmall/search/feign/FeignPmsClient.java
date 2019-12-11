package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/10
 * \* Time: 11:15
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */

@FeignClient("pms-service")
public interface FeignPmsClient extends GmallPmsApi {


}
