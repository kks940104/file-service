package org.anonymous.global.configs;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.anonymous.member.filters.LoginFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity // 기본 보안 정책 활성화 -> 통제.
@EnableMethodSecurity // @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final LoginFilter loginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /**
         * SessionCreation
         *      - ALWAYS : 서버가 시작되었을 때 부터 세션 새성, 세션 아이디
         *      - IF_REQUIRED : 세션이 필요한 시점에 세션을 생성(기본값)
         *      - NEVER : 세션 생성 X, 기존에 세션이 존재하면 그거는 사용함.
         *      - STATELESS : 세션 생성 X, 기존 생성된 세션도 사용 X.
         *      토큰 방식을 사용하기 때문에 세션을 사용하지 않음.
         */

        http.csrf(c -> c.disable())
                .sessionManagement
                        (c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(c -> { // 인증 실패 시 예외처리..
                    c.authenticationEntryPoint((req, res, e) -> {
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }); // 미 로그인한 상태에서 접근한 경우
                    c.accessDeniedHandler((req, res, e) -> {
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }); // 로그인 후 권한이 없는 경우
                })
                .authorizeHttpRequests(c ->  // 권한 및 인증..
                    c.requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                            .anyRequest().permitAll()
                );

        return http.build();
    }
}

















