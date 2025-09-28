package com.wudc.storypool.global.security;

import com.wudc.storypool.global.security.exception.CustomAccessDeniedHandler;
import com.wudc.storypool.global.security.exception.CustomAuthenticationEntryPoint;
import com.wudc.storypool.global.security.filter.JwtAuthFilter;
import com.wudc.storypool.global.security.jwt.JwtParser;
import com.wudc.storypool.global.security.jwt.JwtProperties;
import com.wudc.storypool.global.security.principal.PrincipalDetails;
import com.wudc.storypool.global.security.principal.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class  SecurityConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(
                List.of(
                        "Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method",
                        "Access-Control-Request-Headers", "Authorization", "access_token", "refresh_token"
                ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtParser jwtParser,
        PrincipalDetailsService principalDetailsService
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                // Swagger UI 관련 설정
                .requestMatchers("/swagger/**", "/swagger-ui/**", "/api-docs/**")
                .permitAll()
                // Health check 등 공개 엔드포인트
                .requestMatchers("/", "/health", "/actuator/health", "/actuator/info")
                .permitAll()
                // 기타 Actuator 엔드포인트는 인증 필요 (운영환경에서 보안)
                .requestMatchers("/actuator/**")
                .authenticated()
                // Auth 관련 엔드포인트 (로그인, 회원가입 등)
                .requestMatchers("/api/auth/**")
                .permitAll()
                // 워커 전용 API는 별도의 로직으로 권한 검증
                .requestMatchers(HttpMethod.PATCH, "/api/fairytales/*/status")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/fairytales/notify")
                .permitAll()
                // 나머지 API는 인증 필요
                .anyRequest().authenticated()
            )
            .headers(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .rememberMe(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)

            .sessionManagement((sessionManagement) ->
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .exceptionHandling(handler -> {
                handler.authenticationEntryPoint(authenticationEntryPoint());
                handler.accessDeniedHandler(accessDeniedHandler());
            })

            .addFilterBefore(new JwtAuthFilter(
                jwtParser, principalDetailsService, jwtProperties
            ), UsernamePasswordAuthenticationFilter.class)
            .cors(withDefaults());

        return http.build();
    }
}



