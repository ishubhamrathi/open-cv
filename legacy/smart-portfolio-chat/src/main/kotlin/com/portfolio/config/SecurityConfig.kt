package com.portfolio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import java.util.*

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }
            .csrf { csrf ->
                csrf.disable()
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/api/chat/**",
                        "/api/knowledge/**",
                        "/h2-console/**"
                    ).permitAll()
                    .requestMatchers("/admin/**").authenticated()
                    .anyRequest().permitAll()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .headers { headers ->
                headers.frameOptions { frame ->
                    frame.disable() // Allow H2 console frames
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        return CorsConfigurationSource {
            val config = CorsConfiguration()
            config.allowedOrigins = listOf("*")
            config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            config.allowedHeaders = listOf("*")
            config.maxAge = 3600L
            config
        }
    }
}
