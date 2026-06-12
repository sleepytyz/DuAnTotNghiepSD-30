package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietDotGiamGiaRepo
        extends JpaRepository<ChiTietDotGiamGia,Integer> {
    boolean existsByDotGiamGia_MaGiamGiaAndSanPham_MaSanPham(
            Integer maGiamGia,
            Integer maSanPham
    );
}