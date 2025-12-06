package com.atlasculinary.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
    // Các URI hoàn toàn công khai
    private static final String[] PUBLIC_BASE_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/logout",
            "/api/v1/locations/**",
            "/ws/**",
            "/ws-mobile/**"
    };

    // URI cho phép truy cập GET công khai (tài nguyên chính và tài nguyên con để đọc)
    private static final String[] PUBLIC_GET_URLS = {
            // Tài nguyên chính: Nhà hàng, Món ăn, Review (cho phép /api/v1/restaurants, /api/v1/restaurants/{id}...)
            "/api/v1/restaurants/**",
            "/api/v1/dishes/**",
            "/api/v1/reviews/**",
            "/api/v1/restaurants/*/dishes",
            "/api/v1/restaurants/*/reviews",
            "/api/v1/dishes/*/reviews",
            "/api/v1/restaurants/map-view",
            "/api/v1/restaurants/search"
    };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,  CorsConfig corsConfig) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
              // 1. PUBLIC BASE URLS (Luôn cho phép)
              .requestMatchers(PUBLIC_BASE_URLS).permitAll()

              // 2. PASSWORD MANAGEMENT (Công khai cho quên mật khẩu, xác thực cho đổi mật khẩu)
              .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password", "/api/v1/auth/validate-reset-token", "/api/v1/auth/deeplink/**").permitAll()
              .requestMatchers("/api/v1/auth/change-password").authenticated()

              // 3. PUBLIC GET ACCESS (Đọc dữ liệu công khai)
              .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()

              // 4. All other requests use method-level security (@PreAuthorize with action-based permissions)
              .anyRequest().authenticated()
      )
        .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                    {"status":"error","message":"Unauthorized"}
                """);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                    {"status":"error","message":"Access Denied"}
                """);
                })
        )
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }


}
