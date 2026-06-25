package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Entity.GiamGiaChiTiet;
import com.example.th06876_java202.Entity.GiamGiaChiTietId;
import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Repository.GiamGiaChiTietRepo;
import com.example.th06876_java202.Repository.GiamGiaRepository;
import com.example.th06876_java202.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class GiamGiaService {

    @Autowired
    GiamGiaRepository giamGiaRepository;

    @Autowired
    KhachHangRepository khachHangRepository;

    @Autowired
    GiamGiaChiTietRepo giamGiaChiTietRepo;

    public boolean isDuDieuKienApDung(GiamGia giamGia, BigDecimal tongTienDonHang) {
        return tongTienDonHang.compareTo(giamGia.getDonToiThieu()) >= 0;
    }

    public List<GiamGia> getGiamGia() {
        return giamGiaRepository.findAll();
    }

    public List<GiamGia> getGiamGia3() {
        return giamGiaRepository.findDanhSachCanCapNhat();
    }

    public List<GiamGia> getGiamGia1() {
        return giamGiaRepository.findSoLuongVoucher();
    }

    public void giamSoLuongVoucher(Integer id){
        giamGiaRepository.giamSoLuongVoucher(id);
    }

    public GiamGia save(GiamGia giamGia) {
        return giamGiaRepository.save(giamGia);
    }

    public Optional<GiamGia> getGiamGiaById(int id) {
        return giamGiaRepository.findById(id);
    }

    public void suattt(Integer magg){
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

    public Page<GiamGia> getFilteredGiamGia(String kw, String tt, String lg, LocalDateTime start, LocalDateTime end, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return giamGiaRepository.filterAll(kw, tt, lg, start, end, pageable);
    }

    @Transactional
    public void xoaMem(Integer id) {

        giamGiaRepository.updateTrangThai("Ngừng hoạt động", id);

        // 2. Cập nhật chi tiết
        giamGiaChiTietRepo.updateTrangThaiByMaGiamGia(id, 0);
    }

    public String tinhToanTrangThai(GiamGia gg) {
        LocalDateTime now = LocalDateTime.now();
        if (gg.getNgayBatDau() == null || gg.getNgayKetThuc() == null) return "Ngừng hoạt động";

        if (now.isBefore(gg.getNgayBatDau())) {
            return "Sắp hoạt động";
        } else if (!now.isBefore(gg.getNgayBatDau()) && !now.isAfter(gg.getNgayKetThuc())) {
            return "Hoạt động";
        } else {
            return "Ngừng hoạt động";
        }
    }

    @Transactional
    public void capNhatTrangThaiChoScheduler(String trangThai, int id) {
        giamGiaRepository.updateTrangThai(trangThai, id);
    }

}
