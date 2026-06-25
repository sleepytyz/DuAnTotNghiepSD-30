package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiTietDotGiamGiaRepo extends JpaRepository<ChiTietDotGiamGia,Integer> {
    boolean existsByDotGiamGia_MaGiamGiaAndSanPham_MaSanPham(
            Integer maGiamGia,
            Integer maSanPham
    );

    @Query(value = "SELECT * FROM ChiTietDotGiamGia WHERE " +
            "(:maGiamGia IS NULL OR :maGiamGia = '' OR MaGiamGia = :maGiamGia)",
            nativeQuery = true)
    List<ChiTietDotGiamGia> filterByDotGiamGia(@Param("maGiamGia") String maGiamGia);

    boolean existsByDotGiamGia_MaGiamGiaAndSanPhamChiTiet_MaSanPhamChiTiet(
            Integer maGiamGia,
            Integer maSanPhamChiTiet
    );

    @Query("SELECT DISTINCT ct.sanPham.maSanPham FROM ChiTietDotGiamGia ct WHERE ct.dotGiamGia.maGiamGia = :id")
    List<Integer> findSanPhamByDot(@Param("id") Integer id);

    @Query("SELECT ct.sanPhamChiTiet.maSanPhamChiTiet FROM ChiTietDotGiamGia ct WHERE ct.dotGiamGia.maGiamGia = :id")
    List<Integer> findSanPhamChiTietByDot(@Param("id") Integer id);

}