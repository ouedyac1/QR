package com.dcsic.qrcode.config;

import com.dcsic.qrcode.security.CustomLoginSuccessHandler;
import com.dcsic.qrcode.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomLoginSuccessHandler successHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomLoginSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // Pages publiques
                        .requestMatchers(
                                "/",
                                "/survey/**",
                                "/questions/event/**",
                                "/questions/qrcode/**",
                                "/qrcode/**",
                                "/verify/**",
                                "/show-qrcode/**",
                                "/theme/**",
                                "/questions/ask/**",
                                "/api/files",
                                "/img/**",
                                "/fonts/**",
                                "/bootstrap/**",
                                "/bootbox/**",
                                "/css/**",
                                "/js/**",
                                "/plugins/**",
                                "/v3/api-docs/**",
                                "/font-awesome/**",
                                "/owl-carousel/**",
                                "/login"
                        ).permitAll()

                        // ADMIN uniquement
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Tous les autres doivent être connectés
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler) // ✅ redirection dynamique
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}