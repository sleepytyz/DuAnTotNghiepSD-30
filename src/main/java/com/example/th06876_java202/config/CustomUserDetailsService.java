package com.example.th06876_java202.config;

import com.example.th06876_java202.Entity.TaiKhoan;
import com.example.th06876_java202.Service.TaiKhoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final TaiKhoanService taiKhoanService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan myUser = taiKhoanService.findUserByTenDangNhap(username);

        if (myUser == null) {
            throw new UsernameNotFoundException("username not found");
        }
        return new User(myUser.getTenDangNhap(), myUser.getMatKhau(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + myUser.getVaiTro())));
    }
}
