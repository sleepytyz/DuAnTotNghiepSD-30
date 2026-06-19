package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangService {
    private final KhachHangRepository khachHangRepo;

    public Page<KhachHang> getAllKhachHangPagin(Pageable pageable) {
        return khachHangRepo.findAll(pageable);
    }

    public List<KhachHang> getAllKhachHang() {
        return khachHangRepo.findAll();
    }

    public List<KhachHang> findBySdt(String sdt) {
        return khachHangRepo.findBySdt(sdt);
    }

    public List<KhachHang> findByHangKH(String tt) {
        return khachHangRepo.findByHangKhachHang(tt);
    }

    public KhachHang getKhachHangById(Integer maKH) {
        return khachHangRepo.findById(maKH).orElse(null);
    }

    public void save(KhachHang khachHang) {
        khachHangRepo.save(khachHang);
    }

    public void lock(Integer maKH) {
        KhachHang kh = khachHangRepo.findById(  maKH).orElse(null);
        if (kh != null) {
            kh.setTrangThai(false);  // false = khóa
            khachHangRepo.save(kh);
        }
    }

    public void unlock(Integer maKH) {
        KhachHang kh = khachHangRepo.findById(maKH).orElse(null);
        if (kh != null) {
            kh.setTrangThai(true);  // true = hoạt động
            khachHangRepo.save(kh);
        }
    }

    public Page<KhachHang> findBySdt(String sdt, Pageable pageable) {
        return khachHangRepo.findBySdtContaining(sdt, pageable);
    }

    public Page<KhachHang> findByHangKH(String hang, Pageable pageable) {
        return khachHangRepo.findByHangKhachHang(hang, pageable);
    }

    public boolean existsBySoDienThoai(String soDienThoai) {
        return khachHangRepo.existsBySdt(soDienThoai);
    }
}
