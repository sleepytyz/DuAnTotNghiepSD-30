package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Repository.GiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GiamGiaService {

    @Autowired
    GiamGiaRepository giamGiaRepository;

    private final Random random = new Random();

    // ===== TẠO MÃ GIẢM GIÁ TỰ ĐỘNG =====
    public String generateMaGiamGia() {
        String code;
        boolean exists;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            int randomNumber = 1000 + random.nextInt(9000);
            code = "GG" + randomNumber;
            exists = giamGiaRepository.existsById(code);
            attempts++;

            if (attempts > maxAttempts) {
                code = "GG" + System.currentTimeMillis();
                break;
            }
        } while (exists);

        return code;
    }

    // ===== LƯU - TỰ ĐỘNG SET NGÀY TẠO =====
    public GiamGia save(GiamGia giamGia) {
        if (giamGia.getNgayTao() == null) {
            giamGia.setNgayTao(LocalDateTime.now());
        }
        return giamGiaRepository.save(giamGia);
    }

    // ===== LẤY TẤT CẢ SẮP XẾP THEO NGÀY TẠO GIẢM DẦN =====
    public List<GiamGia> getGiamGia() {
        return giamGiaRepository.findAllOrderByNgayTaoDesc();
    }

    // ===== CÁC METHOD KHÁC =====
    public List<GiamGia> getGiamGia3() {
        return giamGiaRepository.findDanhSachCanCapNhat();
    }

    // ⭐ SỬA: Lấy TẤT CẢ voucher (không filter)
    public List<GiamGia> getGiamGia1() {
        return giamGiaRepository.findSoLuongVoucher();
    }

    // ⭐ THÊM: Lấy voucher đang hoạt động
    public List<GiamGia> getVoucherDangHoatDong() {
        return giamGiaRepository.findVoucherDangHoatDong();
    }

    public void giamSoLuongVoucher(String id){
        giamGiaRepository.giamSoLuongVoucher(id);
    }

    public Optional<GiamGia> getGiamGiaById(String id) {
        return giamGiaRepository.findById(id);
    }

    public void suattt(String magg){
        giamGiaRepository.updateGiamGiaaa(magg);
    }

    public List<GiamGia> timkiem(String keyword) {
        return giamGiaRepository.timkiem(keyword);
    }

    public List<GiamGia> loclg(String keyword) {
        return giamGiaRepository.getGiamGia(keyword);
    }

    public List<GiamGia> loctt(String keyword) {
        return giamGiaRepository.loctt(keyword);
    }

    public List<GiamGia> locng(LocalDateTime date, LocalDateTime time) {
        return giamGiaRepository.timkiemngay(date,time);
    }

    public boolean existsTenGiamGia(String tenChuongTrinh) {
        return giamGiaRepository.existsByTenGiamGia(tenChuongTrinh);
    }

    @Transactional
    public void activateVoucher(String id) {
        giamGiaRepository.activateVoucher(id);
    }

    // ===== FILTER WITH LOAI_AP_DUNG =====
    public Page<GiamGia> getFilteredGiamGia(String kw, String tt, String lg, Integer loaiApDung,
                                            LocalDateTime start, LocalDateTime end, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return giamGiaRepository.filterAll(kw, tt, lg, loaiApDung, start, end, pageable);
    }

    public List<GiamGia> findAllFiltered(String kw, String tt, String lg, Integer loaiApDung,
                                         LocalDateTime start, LocalDateTime end) {
        return giamGiaRepository.findAllFiltered(kw, tt, lg, loaiApDung, start, end);
    }

    @Transactional
    public void xoaMem(String id) {
        giamGiaRepository.updateTrangThai("Ngừng hoạt động", id);
    }

    public String tinhToanTrangThai(GiamGia gg) {
        if (gg == null) return "Ngừng hoạt động";

        LocalDateTime now = LocalDateTime.now();

        // ⭐ KIỂM TRA VÔ HẠN TRƯỚC - NẾU VÔ HẠN THÌ KHÔNG CẦN KIỂM TRA SỐ LƯỢNG
        if (gg.getIsVoHan() != null && gg.getIsVoHan()) {
            // Vô hạn: chỉ cần kiểm tra ngày tháng
            if (gg.getNgayBatDau() != null && gg.getNgayKetThuc() != null) {
                if (now.isBefore(gg.getNgayBatDau())) {
                    return "Sắp hoạt động";
                } else if (now.isAfter(gg.getNgayKetThuc())) {
                    return "Ngừng hoạt động";
                } else {
                    return "Hoạt động";
                }
            }
            return "Hoạt động"; // Nếu không có ngày tháng, mặc định hoạt động
        }

        // ⭐ KHÔNG VÔ HẠN: KIỂM TRA SỐ LƯỢNG + NGÀY THÁNG
        if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) {
            return "Hết lượt";
        }

        if (gg.getNgayBatDau() != null && gg.getNgayKetThuc() != null) {
            if (now.isBefore(gg.getNgayBatDau())) {
                return "Sắp hoạt động";
            } else if (now.isAfter(gg.getNgayKetThuc())) {
                return "Ngừng hoạt động";
            } else {
                return "Hoạt động";
            }
        }

        return "Hoạt động";
    }

    @Transactional
    public void capNhatTrangThaiChoScheduler(String trangThai, String id) {
        giamGiaRepository.updateTrangThai(trangThai, id);
    }

    @Transactional
    public void updateTrangThaiToStop(String id) {
        giamGiaRepository.updateTrangThaiToStop(id);
    }
}