package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/13
 * \* Time: 12:45
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
