package com.atguigu.gmall.gateway.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/17
 * \* Time: 11:22
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "gmall.jwt")
public class JwtProperties {

    private String pubKeyPath;// 公钥

    private PublicKey publicKey; // 公钥


    private String cookieName; // cookie名称

    /**
     * @PostContruct：在构造方法执行之后执行该方法
     */
    @PostConstruct
    public void init() {
        try {
            File pubKey = new File(pubKeyPath);
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException();
        }
    }
}
