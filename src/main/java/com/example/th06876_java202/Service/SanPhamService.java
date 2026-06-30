package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Repository.SanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public SanPhamService(SanPhamRepository sanPhamRepository) {
        this.sanPhamRepository = sanPhamRepository;
    }

    // ===== LẤY TẤT CẢ SẢN PHẨM =====
    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    // ===== LẤY SẢN PHẨM THEO ID =====
    public Optional<SanPham> findById(String id) {
        return sanPhamRepository.findById(id);
    }

    // ===== LẤY SẢN PHẨM THEO TÊN =====
    public SanPham findByTenSanPham(String tenSanPham) {
        return sanPhamRepository.findByTenSanPham(tenSanPham).orElse(null);
    }

    // ===== LẤY SẢN PHẨM MỚI NHẤT =====
    public List<SanPham> layMoiNhat() {
        return sanPhamRepository.findTop8ByTrangThaiTrueOrderByMaSanPhamDesc();
    }

    // ===== LẤY SẢN PHẨM THEO TRẠNG THÁI =====
    public List<SanPham> findBytt(String ten) {
        return sanPhamRepository.getallbyTrangThai(ten);
    }

    // ===== LẤY TẤT CẢ SẢN PHẨM CÓ PHÂN TRANG =====
    public Page<SanPham> getallpage(Pageable pageable) {
        return sanPhamRepository.findAllByOrderByNgayTaoDesc(pageable);
    }

    // ===== TÌM KIẾM SẢN PHẨM =====
    public Page<SanPham> searchSanPham(String maDanhMuc, Boolean tt, String maTH, String maKG, String t, Pageable pageable) {
        String keyword = (t == null || t.trim().isEmpty()) ? null : t.trim();
        return sanPhamRepository.searchSanPham(maDanhMuc, tt, maTH, maKG, keyword, pageable);
    }

    // ===== TÌM KIẾM SẢN PHẨM (KHÔNG PHÂN TRANG) =====
    public List<SanPham> findAllWithFilters(String maDanhMuc, Boolean tt, String maTH, String maKG, String t) {
        String keyword = (t == null || t.trim().isEmpty()) ? null : t.trim();
        return sanPhamRepository.findAllWithFilters(maDanhMuc, tt, maTH, maKG, keyword);
    }

    // ===== LƯU SẢN PHẨM =====
    public SanPham save(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    // ===== XÓA SẢN PHẨM =====
    public void delete(String maSanPham) {
        sanPhamRepository.deleteById(maSanPham);
    }

    // ===== CẬP NHẬT TRẠNG THÁI =====
    public void updateTrangThai(String maSanPham, boolean trangThai) {
        sanPhamRepository.updateTrangThai(maSanPham, trangThai);
    }

    // ===== KIỂM TRA TỒN TẠI =====
    public boolean existsByTenSanPham(String tenSanPham) {
        return sanPhamRepository.existsByTenSanPham(tenSanPham);
    }

    public boolean isTenSanPhamDuplicate(String ten) {
        if (ten == null) return false;
        String normalizedName = ten.trim().replaceAll("\\s+", " ");
        return sanPhamRepository.existsByTenSanPhamIgnoreCase(normalizedName);
    }

    // ===== ĐẾM SỐ LƯỢNG THEO TRẠNG THÁI =====
    public long countByTrangThai(boolean trangThai) {
        return sanPhamRepository.countByTrangThai(trangThai);
    }
}