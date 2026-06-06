package com.example.duantotnghiep.service;

import com.example.duantotnghiep.model.Account;

import java.util.List;

public interface AccountService {
    List<Account> getAll();
    List<Account> search(String keyword);
    Account save(Account account);
    void lock(Integer id);
    void unlock(Integer id);
    void delete(Integer id);
}