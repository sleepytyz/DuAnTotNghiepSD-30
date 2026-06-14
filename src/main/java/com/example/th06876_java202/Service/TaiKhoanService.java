package com.example.th06876_java202.Service;



import com.example.th06876_java202.Entity.TaiKhoan;
import com.example.th06876_java202.Repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {

//    private final Repo_Account repo;
//
//    public AccountServiceImpl(Repo_Account repo) {
//        this.repo = repo;
//    }
//
//    public List<TaiKhoan> getAll() {
//        return repo.findAll();
//    }
//
//    public List<TaiKhoan> search(String keyword) {
//        if(keyword == null || keyword.trim().isEmpty()){
//            return repo.findAll();
//        }
//        return repo.findByTenDangNhapContaining(keyword);
//    }
//
//    public TaiKhoan save(TaiKhoan account) {
//        return repo.save(account);
//    }
//
//    public void lock(Integer id) {
//        TaiKhoan account = repo.findById(id).orElse(null);
//        if(account != null){
//            account.setTrangThai(false);
//            repo.save(account);
//        }
//    }
//
//    public void unlock(Integer id) {
//        TaiKhoan account = repo.findById(id).orElse(null);
//        if(account != null){
//            account.setTrangThai(true);
//            repo.save(account);
//        }
//    }
//
//    public void delete(Integer id){
//        TaiKhoan account = repo.findById(id).orElse(null);
//        if(account != null){
//            repo.delete(account);
//        }
//    }
//
//    public TaiKhoan dangnhap(String tenDangNhap, String matkhau) {
//        return repo.findTenDangNhap(tenDangNhap,matkhau);
//    }
//
//    public TaiKhoan findByTenDangNhap(String tendn) {
//        return repo.findByTenDangNhap(tendn);
//    }

    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TaiKhoan> fetchUsers() {
        List<TaiKhoan> tkList = taiKhoanRepository.findAll();
        return tkList;
    }

    public void createUser(TaiKhoan taiKhoan) {
        taiKhoan.setMatKhau(passwordEncoder.encode(taiKhoan.getMatKhau()));
        taiKhoan.setVaiTro("USER");
        taiKhoan.setTrangThai(true);
        taiKhoanRepository.save(taiKhoan);

    }

    public TaiKhoan findUserById(int id) {
        Optional<TaiKhoan> tk = taiKhoanRepository.findById(id);
        return tk.orElse(null);
    }

    public void updateUser(TaiKhoan taiKhoan) {
        taiKhoan.setMatKhau(passwordEncoder.encode(taiKhoan.getMatKhau()));
        taiKhoanRepository.save(taiKhoan);
    }

    public void deleteUserById(int id) {
        taiKhoanRepository.deleteById(id);
    }

    public TaiKhoan findUserByTenDangNhap(String name) {
        Optional<TaiKhoan> userOpt = taiKhoanRepository.findByTenDangNhap(name);
        return userOpt.orElse(null);
    }

    public boolean isTenDangNhapExist(String name) {
        return taiKhoanRepository.existsByTenDangNhap(name);
    }

    public void handleRegister(TaiKhoan taiKhoan) {
        taiKhoan.setMatKhau(passwordEncoder.encode(taiKhoan.getMatKhau()));
        taiKhoan.setVaiTro("USER");

        taiKhoanRepository.save(taiKhoan);
    }
}