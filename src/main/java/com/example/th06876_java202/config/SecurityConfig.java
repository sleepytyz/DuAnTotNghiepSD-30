package com.example.th06876_java202.config;

import com.example.th06876_java202.Service.TaiKhoanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(TaiKhoanService taiKhoanService) {
        return new CustomUserDetailsService(taiKhoanService);
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                //ai cũng vào đc, ko cần đăng nhập
                .requestMatchers("/", "/login", "/register", "/forgot").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                //đăng nhập rồi mới vào đc
//                .requestMatchers("/san-pham/**").authenticated()

                //chỉ user mới vào đc
                .requestMatchers("/user/**").hasRole("USER")

                //admin và staff mới vào đc
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

                //chỉ admin mới vào đc
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
        //
        http.sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired"));
//        http.httpBasic(withDefaults());
        http.rememberMe(r -> r
                .key("mySecretKey_12345")                    // BẮT BUỘC
                .rememberMeParameter("remember-me")          // Tên checkbox
                .tokenValiditySeconds(30 * 24 * 60 * 60)     // 30 ngày
                .userDetailsService(userDetailsService(null)) // Dùng UserDetailsService
                .useSecureCookie(false)                      // false vì dùng HTTP
        );
        return http.build();
    }
}
