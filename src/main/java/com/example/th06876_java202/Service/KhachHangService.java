package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

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
        return khachHangRepo.findBySdtt(sdt);
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
            kh.setTrangThai(false);
            khachHangRepo.save(kh);
        }
    }

    public KhachHang getReferenceById(Integer maKH) {
        return khachHangRepo.getReferenceById(maKH);
    }

    public void validateKhachHang(KhachHang kh, BindingResult result) {
        // 1. Kiểm tra SĐT
        if (kh.getSdt() != null) {
            String sdtClean = kh.getSdt().trim();
            boolean sdtExists = (kh.getMaKH() == null)
                    ? khachHangRepo.existsBySdt(sdtClean)
                    : khachHangRepo.existsBySdtAndMaKHNot(sdtClean, kh.getMaKH());

            if (sdtExists) {
                result.rejectValue("sdt", "error.kh", "Số điện thoại đã tồn tại!");
            }
        }

        // 2. Kiểm tra Email
        if (kh.getEmail() != null && !kh.getEmail().trim().isEmpty()) {
            String emailClean = kh.getEmail().trim();
            boolean emailExists = (kh.getMaKH() == null)
                    ? khachHangRepo.existsByEmail(emailClean)
                    : khachHangRepo.existsByEmailAndMaKHNot(emailClean, kh.getMaKH());

            if (emailExists) {
                result.rejectValue("email", "error.kh", "Email này đã được sử dụng!");
            }
        }
    }

    public void unlock(Integer maKH) {
        KhachHang kh = khachHangRepo.findById(maKH).orElse(null);
        if (kh != null) {
            kh.setTrangThai(true);  // true = hoạt động
            khachHangRepo.save(kh);
        }
    }

    public Page<KhachHang> searchByPhone(String sdt, int page) {

        Pageable pageable = PageRequest.of(page, 5);

        if (sdt == null || sdt.trim().isEmpty()) {
            return khachHangRepo.findAll(pageable);
        }

        return khachHangRepo.findBySdtContaining(sdt, pageable);
    }

    public Page<KhachHang> findBySdt(String sdt, Pageable pageable) {
        return khachHangRepo.findBySdtContaining(sdt, pageable);
    }

    public boolean existsBySoDienThoai(String soDienThoai) {
        return khachHangRepo.existsBySdt(soDienThoai);
    }
}
