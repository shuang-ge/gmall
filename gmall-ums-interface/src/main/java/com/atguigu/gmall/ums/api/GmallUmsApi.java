package com.atguigu.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/16
 * \* Time: 18:40
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public interface GmallUmsApi {

    @PostMapping("ums/member/query")
    public Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password);
}
