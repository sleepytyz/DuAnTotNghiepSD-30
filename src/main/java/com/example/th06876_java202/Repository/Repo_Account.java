package com.example.th06876_java202.Repository;


import com.example.th06876_java202.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface Repo_Account extends JpaRepository<Account, Integer> {
    List<Account> findByTenDangNhapContaining(String keyword);
    List<Account> findByVaiTro(String vaiTro);
    List<Account> findByTrangThai(Boolean trangThai);
}