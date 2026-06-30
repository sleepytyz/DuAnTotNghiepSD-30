package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ChiTietDotGiamGia;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChiTietDotGiamGiaRepo extends JpaRepository<ChiTietDotGiamGia, Integer> {

    boolean existsByDotGiamGia_MaGiamGiaAndSanPham_MaSanPham(
            String maGiamGia,
            String maSanPham
    );

    @Query(value = "SELECT * FROM ChiTietDotGiamGia WHERE " +
            "(:maGiamGia IS NULL OR :maGiamGia = '' OR MaGiamGia = :maGiamGia)",
            nativeQuery = true)
    List<ChiTietDotGiamGia> filterByDotGiamGia(@Param("maGiamGia") String maGiamGia);


    @Query("SELECT DISTINCT ct.sanPham.maSanPham FROM ChiTietDotGiamGia ct WHERE ct.dotGiamGia.maGiamGia = :id")
    List<String> findSanPhamByDot(@Param("id") String id);

    @Query("SELECT ct.sanPhamChiTiet.maSanPhamChiTiet FROM ChiTietDotGiamGia ct WHERE ct.dotGiamGia.maGiamGia = :id")
    List<String> findSanPhamChiTietByDot(@Param("id") String id);

    // Trong ChiTietDotGiamGiaRepo.java
    @Modifying
    @Query("DELETE FROM ChiTietDotGiamGia c WHERE c.dotGiamGia.maGiamGia = :maGiamGia AND c.sanPham.maSanPham = :maSanPham")
    void deleteByDotGiamGia_MaGiamGiaAndSanPham_MaSanPham(
            @Param("maGiamGia") String maGiamGia,
            @Param("maSanPham") String maSanPham
    );

    // ===== 3. PHƯƠNG THỨC XOÁ CHI TIẾT THEO MÃ ĐỢT GIẢM GIÁ =====
    @Modifying
    @Transactional
    @Query("DELETE FROM ChiTietDotGiamGia c WHERE c.dotGiamGia.maGiamGia = :maGiamGia")
    void deleteByDotGiamGia_MaGiamGia(@Param("maGiamGia") String maGiamGia);

    // Trong ChiTietDotGiamGiaRepo.java
    @Modifying
    @Query("DELETE FROM ChiTietDotGiamGia c WHERE c.dotGiamGia.maGiamGia = :maGiamGia AND c.sanPham.maSanPham = :maSanPham")
    void deleteByMaGiamGiaAndMaSanPham(@Param("maGiamGia") String maGiamGia, @Param("maSanPham") String maSanPham);

    // Trong ChiTietDotGiamGiaRepo.java
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChiTietDotGiamGia c " +
            "WHERE c.dotGiamGia.maGiamGia = :maGiamGia AND c.sanPhamChiTiet.maSanPhamChiTiet = :maSanPhamChiTiet")
    boolean existsByDotGiamGia_MaGiamGiaAndSanPhamChiTiet_MaSanPhamChiTiet(
            @Param("maGiamGia") String maGiamGia,
            @Param("maSanPhamChiTiet") String maSanPhamChiTiet
    );

    boolean existsByDotGiamGia_MaGiamGiaAndSanPham_MaSanPhamAndSanPhamChiTiet_MaSanPhamChiTiet(
            String maGiamGia, String maSanPham, String maSanPhamChiTiet
    );

    // Trong ChiTietDotGiamGiaRepo.java
    List<ChiTietDotGiamGia> findBySanPhamChiTiet_MaSanPhamChiTiet(String maSanPhamChiTiet);

    @Query("SELECT d.giaTriGiam FROM ChiTietDotGiamGia c JOIN c.dotGiamGia d " +
            "WHERE c.sanPham.maSanPham = :maSanPham AND d.trangThai = 'Hoạt động' AND :today BETWEEN d.ngayBatDau AND d.ngayKetThuc " +
            "ORDER BY d.giaTriGiam DESC")
    List<java.math.BigDecimal> findActiveDiscountPercentBySanPham(@Param("maSanPham") String maSanPham, @Param("today") java.time.LocalDate today);

    @Query("SELECT d.giaTriGiam FROM ChiTietDotGiamGia c JOIN c.dotGiamGia d " +
            "WHERE c.sanPhamChiTiet.maSanPhamChiTiet = :maSPCT AND d.trangThai = 'Hoạt động' AND :today BETWEEN d.ngayBatDau AND d.ngayKetThuc " +
            "ORDER BY d.giaTriGiam DESC")
    List<java.math.BigDecimal> findActiveDiscountPercentBySanPhamChiTiet(@Param("maSPCT") String maSPCT, @Param("today") java.time.LocalDate today);

}