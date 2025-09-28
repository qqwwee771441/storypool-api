package com.wudc.storypool.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";
        String serviceTokenSchemeName = "serviceToken";
        
        SecurityRequirement jwtSecurityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        SecurityRequirement serviceSecurityRequirement = new SecurityRequirement().addList(serviceTokenSchemeName);
        
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("사용자 인증용 JWT 토큰"));

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(jwtSecurityRequirement)
                .addSecurityItem(serviceSecurityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("StoryPool API")
                .description("StoryPool - 어린이용 동화 생성 및 커뮤니티 서비스 API\n\n" +
                        "## 주요 기능\n" +
                        "- 사용자 인증 및 관리\n" +
                        "- 이야기 초안 작성 및 관리\n" +
                        "- AI 동화 생성 및 상태 관리\n" +
                        "- 게시글 및 댓글 시스템\n" +
                        "- 푸시 알림 및 디바이스 관리\n" +
                        "- 파일 업로드 (S3 Presigned URL)\n\n" +
                        "## 인증 방식\n" +
                        "- **JWT Bearer Token**: 일반 사용자 API 인증\n")
                .version("1.0.0");
    }
}