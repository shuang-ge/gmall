package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.feign.GmallSmsApi;
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
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
