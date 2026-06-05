package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Account;
import com.example.duantotnghiep.repository.Repo_Account;
import com.example.duantotnghiep.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final Repo_Account repo;

    public AccountServiceImpl(Repo_Account repo) {
        this.repo = repo;
    }

    @Override
    public List<Account> getAll() {
        return repo.findAll();
    }

    @Override
    public List<Account> search(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()){
            return repo.findAll();
        }
        return repo.findByTenDangNhapContaining(keyword);
    }

    @Override
    public Account save(Account account) {
        return repo.save(account);
    }

    @Override
    public void lock(Integer id) {
        Account account = repo.findById(id).orElse(null);
        if(account != null){
            account.setTrangThai(false);
            repo.save(account);
        }
    }

    @Override
    public void unlock(Integer id) {
        Account account = repo.findById(id).orElse(null);
        if(account != null){
            account.setTrangThai(true);
            repo.save(account);
        }
    }

    @Override
    public void delete(Integer id){
        Account account = repo.findById(id).orElse(null);
        if(account != null){
            repo.delete(account);
        }
    }
}