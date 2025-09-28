package com.wudc.storypool.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuer;
    private String accessSecret;
    private String refreshSecret;
    private Long tokenExpire;
    private Long refreshTokenExpire;
    private Boolean isSecure;
    private String sameSite;
}
