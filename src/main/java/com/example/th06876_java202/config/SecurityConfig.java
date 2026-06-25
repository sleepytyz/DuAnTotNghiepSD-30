package com.example.th06876_java202.config;

import com.example.th06876_java202.Service.TaiKhoanService;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAsync
public class SecurityConfig {
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(100 * 1024 * 1024);
        });
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(TaiKhoanService taiKhoanService) {
        return new CustomUserDetailsService(taiKhoanService);
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                // ai cũng vào đc, ko cần đăng nhập
                .requestMatchers("/", "/login", "/register", "/forgot").permitAll()

                // ✅ Cho phép truy cập static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                // ✅ QUAN TRỌNG: Cho phép truy cập ảnh từ thư mục uploads
                .requestMatchers("/uploads/**", "/images/sanpham/**").permitAll()

                // chỉ user mới vào đc
                .requestMatchers("/user/**").hasRole("USER")

                // admin và staff mới vào đc
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

                // chỉ admin mới vào đc
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll());

        // Cấu hình LOGOUT
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
        );

        http.exceptionHandling(exception -> exception.accessDeniedPage("/accessDenied"));

        http.sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired"));

        http.rememberMe(r -> r
                .key("mySecretKey_12345")
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
                .useSecureCookie(false)
        );

        // ✅ Cấu hình CSRF - Bỏ qua cho các URL upload file
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/sanpham/add", "/sanpham/add/**")
                .ignoringRequestMatchers("/uploads/**")
                .ignoringRequestMatchers("/khach-hang/xoa-dia-chi/**")
        );

        return http.build();
    }
}