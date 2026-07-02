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
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // [SỬA] Bắt buộc phải có bean này khi dùng .maximumSessions(...) trong sessionManagement,
    // nếu không Spring Security sẽ không nhận được sự kiện session bị hủy (logout, hết hạn,...)
    // -> SessionRegistry bị sai lệch -> có thể coi phiên đang hợp lệ là "đã hết hạn" và đá
    // người dùng về lại trang đăng nhập một cách ngẫu nhiên, dù họ vừa mới đăng nhập xong.
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
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
                .requestMatchers("/", "/login", "/register", "/forgot", "/accessDenied").permitAll()

                // ✅ Cho phép truy cập static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/storefront/**").permitAll()

                // ✅ QUAN TRỌNG: Cho phép truy cập ảnh từ thư mục uploads
                .requestMatchers("/uploads/**", "/images/sanpham/**").permitAll()

                // === Website bán hàng FS Shoes: xem sản phẩm & giỏ hàng không cần đăng nhập ===
                .requestMatchers("/san-pham", "/san-pham/**").permitAll()
                .requestMatchers("/gio-hang", "/gio-hang/**").permitAll()

                // === Cần đăng nhập bằng tài khoản khách hàng (USER) ===
                .requestMatchers("/thanh-toan", "/thanh-toan/**").hasRole("USER")
                .requestMatchers("/ca-nhan", "/ca-nhan/**").hasRole("USER")
                .requestMatchers("/user/**").hasRole("USER")

                // === Khu vực dùng chung cho cả ADMIN và STAFF: bán hàng tại quầy,
                // đơn hàng, hóa đơn, khách hàng, chấm công, hồ sơ cá nhân
                // (đây cũng chính là các mục có trong sidebar-staff) ===
                .requestMatchers(
                        "/banhang/**", "/donhang/**", "/hoa-don/**", "/khach-hang/**",
                        "/cham-cong/**", "/nv-ca-nhan/**", "/staff/**"
                ).hasAnyRole("ADMIN", "STAFF")

                // === Khu vực quản trị: sản phẩm, thuộc tính sản phẩm, nhân viên,
                // nhập hàng, giảm giá, giao ca & thống kê (chỉ ADMIN) ===
                .requestMatchers(
                        "/chatlieu/**", "/chi-tiet-dot-giam-gia/**", "/danhmucsp/**",
                        "/detail/**", "/giamgia/**", "/kichthuoc/**", "/kieugiay/**",
                        "/mausac/**", "/nhan-vien/**", "/nhap-hang/**", "/sanphamct/**",
                        "/sanphamha/**", "/sanpham/**", "/tai-khoan/**", "/thong-ke/**",
                        "/thuonghieu/**", "/giao-ca/**", "/admin/**"
                ).hasRole("ADMIN")

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

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        // API bán hàng
                        "/banhang/**",
                        // Upload file
                        "/sanpham/add", "/sanpham/add/**",
                        "/uploads/**",
                        "/khach-hang/xoa-dia-chi/**",
                        // API địa chỉ
                        "/banhang/themdiachi",
                        "/banhang/suadiachi/**",
                        "/banhang/xoadiachi/**",
                        "/banhang/chondiachi",
                        "/banhang/setdefault/**",
                        "/banhang/diachi/**"
                )
        );

        return http.build();
    }
}