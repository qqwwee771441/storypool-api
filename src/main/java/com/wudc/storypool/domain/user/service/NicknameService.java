package com.wudc.storypool.domain.user.service;

import de.huxhorn.sulky.ulid.ULID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class NicknameService {

    private static final String[] ADJECTIVES = {
        "용감한", "지혜로운", "친절한", "활발한", "차분한", "밝은", "신비한", "멋진", 
        "재미있는", "따뜻한", "시원한", "빠른", "느린", "강한", "부드러운", "달콤한"
    };

    private static final String[] ANIMALS = {
        "호랑이", "사자", "고양이", "강아지", "토끼", "여우", "늑대", "곰", 
        "팬더", "코알라", "다람쟁이", "햄스터", "독수리", "올빼미", "펭귄", "돌고래"
    };

    private final SecureRandom random = new SecureRandom();

    public String generateNickname(String email) {
        // 1. 이메일의 @ 앞부분을 기본으로 사용
        String emailPrefix = extractEmailPrefix(email);
        
        // 2. 이메일 앞부분이 2-8자인 경우 그대로 사용 + 숫자 추가
        if (emailPrefix.length() >= 2 && emailPrefix.length() <= 8) {
            String randomNum = String.valueOf(1000 + random.nextInt(9000)); // 4자리 숫자
            return emailPrefix + randomNum;
        }
        
        // 3. 이메일 앞부분이 부적절한 경우 랜덤 닉네임 생성
        return generateRandomNickname();
    }

    private String extractEmailPrefix(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf("@")).replaceAll("[^a-zA-Z0-9가-힣]", "");
    }

    private String generateRandomNickname() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String animal = ANIMALS[random.nextInt(ANIMALS.length)];
        
        // ULID의 마지막 4자리를 숫자로 사용
        ULID ulid = new ULID();
        String ulidStr = ulid.nextULID();
        String suffix = ulidStr.substring(ulidStr.length() - 4);
        
        return adjective + animal + suffix;
    }

    public String generateUniqueNickname(String email) {
        String baseNickname = generateNickname(email);
        
        // 닉네임 길이가 10자를 초과하는 경우 자르기
        if (baseNickname.length() > 10) {
            baseNickname = baseNickname.substring(0, 10);
        }
        
        // 최소 2자 보장
        if (baseNickname.length() < 2) {
            baseNickname = generateRandomNickname();
            if (baseNickname.length() > 10) {
                baseNickname = baseNickname.substring(0, 10);
            }
        }
        
        log.info("Generated nickname for email {}: {}", email, baseNickname);
        return baseNickname;
    }
}