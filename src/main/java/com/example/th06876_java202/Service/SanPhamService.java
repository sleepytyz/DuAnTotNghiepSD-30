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

    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    public SanPham save(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    public void updateTrangThai(String maSanPham, boolean trangThai) {
        sanPhamRepository.updateTrangThai(maSanPham, trangThai);
    }

    public SanPham findByTenSanPham(String tenSanPham) {
        return sanPhamRepository.findByTenSanPham(tenSanPham).orElse(null);
    }

    public Optional<SanPham> findById(String id) {
        return sanPhamRepository.findById(id);
    }

    public boolean existsByTenSanPham(String TenSanPham) {
        return sanPhamRepository.existsByTenSanPham(TenSanPham);
    }

    public Page<SanPham> searchSanPham(String maDanhMuc, Boolean tt, String maTH, String maKG, String t, Pageable pageable) {
        String keyword = (t == null || t.trim().isEmpty()) ? null : t.trim();
        return sanPhamRepository.searchSanPham(maDanhMuc, tt, maTH, maKG, keyword, pageable);
    }

    public List<SanPham> findBytt(String ten) {
        return sanPhamRepository.getallbyTrangThai(ten);
    }

    public Page<SanPham> getallpage(Pageable pageable) {
        return sanPhamRepository.findAllByOrderByNgayTaoDesc(pageable);
    }

    public boolean isTenSanPhamDuplicate(String ten) {
        if (ten == null) return false;
        String normalizedName = ten.trim().replaceAll("\\s+", " ");
        return sanPhamRepository.existsByTenSanPhamIgnoreCase(normalizedName);
    }

    public List<SanPham> layMoiNhat() {
        return sanPhamRepository.findTop8ByTrangThaiTrueOrderByMaSanPhamDesc();
    }

}
    public long countByTrangThai(boolean trangThai) {
        return sanPhamRepository.countByTrangThai(trangThai);
    }

    public List<SanPham> findAllWithFilters(String maDanhMuc, Boolean tt, String maTH, String maKG, String t) {
        String keyword = (t == null || t.trim().isEmpty()) ? null : t.trim();
        return sanPhamRepository.findAllWithFilters(maDanhMuc, tt, maTH, maKG, keyword);
    }

}