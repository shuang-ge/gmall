package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/16
 * \* Time: 19:17
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public class JwtTest {
    private static final String pubKeyPath = "E:\\Git\\gmallproject\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\Git\\gmallproject\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "sf3423jsdf#3$@FDS32");
    }

    //@Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzAxMjEyODZ9.GioCiqMt_ZcN6_RAuDBcOzcHQ5WdqdhA9QYu-2IqCQqnAef1VyXczEInj1Ef1xo7AvcjxnkIMuZK48OoczUy1iqtPQPDchUzTl03b8h_J3xMBaxOAaKSwMpm20DH25VrTgBExUafyxHwxfOa-PVHW0Kk41KrWDncayzXbZ_lYLoa9Cuvacr8eAFz-ckriIiZ9bRzFkhX-wYHSHFlym2IJRjBRhFtpkN5GLAVsmsdm-yD4eiJXqioWspqXiBSdROsjrTRiFe511yujR0y2ngL9OnZ1QH6bHDQ2WmhPTrswKjjy-HWIxk1FQ7uXtSpPa5diymmPVTWA0clys7R1MK9oQ";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
