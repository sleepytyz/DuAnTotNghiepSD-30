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

    @Query("SELECT d.giaTriGiam FROM ChiTietDotGiamGia c JOIN c.dotGiamGia d " +
            "WHERE c.sanPham.maSanPham = :maSanPham AND d.trangThai = 'Hoạt động' AND :today BETWEEN d.ngayBatDau AND d.ngayKetThuc " +
            "ORDER BY d.giaTriGiam DESC")
    List<java.math.BigDecimal> findActiveDiscountPercentBySanPham(@Param("maSanPham") Integer maSanPham, @Param("today") java.time.LocalDate today);

    @Query("SELECT d.giaTriGiam FROM ChiTietDotGiamGia c JOIN c.dotGiamGia d " +
            "WHERE c.sanPhamChiTiet.maSanPhamChiTiet = :maSPCT AND d.trangThai = 'Hoạt động' AND :today BETWEEN d.ngayBatDau AND d.ngayKetThuc " +
            "ORDER BY d.giaTriGiam DESC")
    List<java.math.BigDecimal> findActiveDiscountPercentBySanPhamChiTiet(@Param("maSPCT") Integer maSPCT, @Param("today") java.time.LocalDate today);

}