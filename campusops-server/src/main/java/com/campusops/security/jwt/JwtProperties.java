package com.campusops.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性，映射 application.yml 中 campusops.security.jwt 节点。
 */
@Data
@Component
@ConfigurationProperties(prefix = "campusops.security.jwt")
public class JwtProperties {

    private String secret = "change-this-dev-secret-change-this-dev-secret";
    private long expirationSeconds = 7200;
}
