package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Repo_Account extends JpaRepository<Account, Integer> {
    List<Account> findByTenDangNhapContaining(String keyword);
    List<Account> findByVaiTro(String vaiTro);
    List<Account> findByTrangThai(Boolean trangThai);
}