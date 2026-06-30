package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// SỬA: String -> Long (vì ID là Integer)
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Long> {

    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "WHERE hdct.maHoaDon.maHoaDon = :maHoaDon " +
            "AND hdct.sanPhamChiTiet.maSanPhamChiTiet = :maSPCT")
    HoaDonChiTiet getallsphd(
            @Param("maHoaDon") String maHoaDon,
            @Param("maSPCT") String maSanPhamChiTiet
    );

    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "WHERE hdct.maHoaDon.maHoaDon = :maHoaDon")
    List<HoaDonChiTiet> getallsphd(@Param("maHoaDon") String maHoaDon);

    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "WHERE hdct.maHoaDon = :hoaDon")
    List<HoaDonChiTiet> findByMaHoaDon(@Param("hoaDon") HoaDon hoaDon);

    List<HoaDonChiTiet> findByMaHoaDon_MaHoaDon(String maHoaDon);

    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "WHERE hdct.maHoaDon.maHoaDon = :maHoaDon " +
            "AND hdct.sanPhamChiTiet.maSanPhamChiTiet = :maSPCT")
    HoaDonChiTiet findByMaHoaDonAndMaSanPhamChiTiet(
            @Param("maHoaDon") String maHoaDon,
            @Param("maSPCT") String maSPCT
    );

    // Lấy sản phẩm với giá mới nhất từ database
    @Query("SELECT hdct.id, hdct.maHoaDon.maHoaDon, " +
            "hdct.sanPhamChiTiet.maSanPhamChiTiet, " +
            "hdct.soLuong, hdct.donGia, " +
            "spct.giaBan, spct.soLuongTon " +
            "FROM HoaDonChiTiet hdct " +
            "JOIN hdct.sanPhamChiTiet spct " +
            "WHERE hdct.maHoaDon.maHoaDon = :maHoaDon")
    List<Object[]> findCartItemsWithLatestPrice(@Param("maHoaDon") String maHoaDon);

    // Method này đã có thể dùng
    HoaDonChiTiet findByMaHoaDon_MaHoaDonAndSanPhamChiTiet_MaSanPhamChiTiet(
            String maHoaDon,
            String maSanPhamChiTiet
    );
}