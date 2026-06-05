package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Repository.SanPhamRepository;
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

    public int suaSanPham(int maSanPham) {
        return sanPhamRepository.updateTrangThaiNgungBan(maSanPham);
    }

    public Optional<SanPham> findById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    public boolean existsByTenSanPham(String TenSanPham) {
        return sanPhamRepository.existsByTenSanPham(TenSanPham);
    }

}
