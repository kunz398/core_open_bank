package com.ocbs.ocbs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppConfig {
    private final JwtAuthFilter jwtAuthFilter;


    // App
    @Value("${app.name}")
    private String appName;

    @Value("${app.short-name}")
    private String shortAppName;

    // Server
    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.servlet.context-path:/}")
    private String serverContextPath;

    // Database
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    // SMTP
    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${spring.mail.from}")
    private String smtpFrom;

    @Value("${spring.profiles.active:}")
    private String activeProfile;



    // --- Getters ---

    public String getAppName() { return appName; }
    public String getShortAppName() { return shortAppName; }
    public int getServerPort() { return serverPort; }
    public String getServerAddress() { return serverAddress; }
    public String getServerContextPath() { return serverContextPath; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getSmtpHost() { return smtpHost; }
     public int getSmtpPort() { return smtpPort; }
    public String getSmtpFrom() { return smtpFrom; }
    public String getActiveProfile() { return activeProfile; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // custom error when no token provided
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                    {
                        "error": "UNAUTHORIZED",
                        "message": "No token provided or token is invalid",
                        "path": "%s"
                    }
                """.formatted(request.getRequestURI()));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                    {
                        "error": "FORBIDDEN",
                        "message": "You do not have permission to access this resource",
                        "path": "%s"
                    }
                """.formatted(request.getRequestURI()));
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // public — no token needed
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh"
                        ).permitAll()

                        // admin only
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                        // everything else needs a valid token
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

        @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
