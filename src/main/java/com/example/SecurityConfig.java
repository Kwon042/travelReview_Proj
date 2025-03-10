package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/static/**", "/css/**", "/js/**", "/images/**", "/main.css"
                        ).permitAll()  // ✅ 정적 리소스 허용
                        .requestMatchers(
                                "/Boards/reviewBoard/**", "/Boards/write", "/Boards/save"
                        ).permitAll()  // ✅ 게시판 접근 및 글 작성 허용
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/mysql/**"))
                        .disable() // ✅ CSRF 비활성화 (정적 리소스 요청 시 필요할 수 있음)
                )
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                // 로그인
                .formLogin((formLogin) -> formLogin
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/"))
                // 로그아웃
                // .invalidateHttpSession(true) >> 로그아웃 시 생성된 사용자 세션도 삭제하도록 처리
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true))
                // 세션 관리 설정 (세션 정책 설정)
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션이 필요할 때만 생성
                    .invalidSessionUrl("/user/login");
        ;
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        // 기본: HttpSessionCsrfTokenRepository를 사용하여 CSRF 토큰을 세션에 저장
        return new HttpSessionCsrfTokenRepository();
    }

}
