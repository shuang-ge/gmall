package com.atguigu.gmall.pms.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.feign.GmallSmsApi;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/4
 * \* Time: 20:22
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@FeignClient("sms-service")
public interface SmsServiceFeign extends GmallSmsApi {

}
