package com.example.th06876_java202.Service;



import com.example.th06876_java202.Entity.Account;
import com.example.th06876_java202.Repository.Repo_Account;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl {

    private final Repo_Account repo;

    public AccountServiceImpl(Repo_Account repo) {
        this.repo = repo;
    }

    public List<Account> getAll() {
        return repo.findAll();
    }

    public List<Account> search(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()){
            return repo.findAll();
        }
        return repo.findByTenDangNhapContaining(keyword);
    }

    public Account save(Account account) {
        return repo.save(account);
    }

    public void lock(Integer id) {
        Account account = repo.findById(id).orElse(null);
        if(account != null){
            account.setTrangThai(false);
            repo.save(account);
        }
    }

    public void unlock(Integer id) {
        Account account = repo.findById(id).orElse(null);
        if(account != null){
            account.setTrangThai(true);
            repo.save(account);
        }
    }

    public void delete(Integer id){
        Account account = repo.findById(id).orElse(null);
        if(account != null){
            repo.delete(account);
        }
    }
}