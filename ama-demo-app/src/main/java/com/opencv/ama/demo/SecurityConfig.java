package com.opencv.ama.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Demo security. The public site and the ask API are open; the admin review UI/API
 * is protected by form login + basic auth. Production hosts should swap this for their own.
 */
@Configuration
@EnableConfigurationProperties(DemoProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(DemoProperties props, PasswordEncoder encoder) {
        UserDetails admin = User.withUsername(props.getAdminUser())
                .password(encoder.encode(props.getAdminPassword()))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/ama/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/ama/ask", "/api/ama/questions/**", "/api/ama/health",
                                "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/admin", "/admin/**", "/api/ama/admin/**").hasRole("ADMIN")
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .httpBasic(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}