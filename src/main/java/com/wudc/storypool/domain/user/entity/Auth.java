package com.wudc.storypool.domain.user.entity;

import jakarta.persistence.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@RedisHash("auth")
public class Auth implements Serializable {
    @Id
    private String id;

    private String authCode;

    @TimeToLive(unit = TimeUnit.MINUTES)
    private int expiredAt;
}
