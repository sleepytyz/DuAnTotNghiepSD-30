package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE SanPhamChiTiet SET TrangThai = N'Ngừng bán' WHERE MaSanPhamChiTiet = ?1",
            nativeQuery = true)
    int updateTrangThaiNgungBan(int maSanPham);

    List<SanPhamChiTiet> findByMaSanPham_MaSanPhamAndMauSac(
            Integer maSP,
            String mauSac);

    @Query( value = "SELECT MIN(ct.maSanPhamChiTiet) FROM SanPhamChiTiet ct JOIN SanPhamHinhAnh ha ON ct.maSanPhamChiTiet = ha.sanPhamChiTiet.maSanPhamChiTiet WHERE ha.laAnhChinh = true GROUP BY ct.maSanPham, ct.mauSac")
    List<SanPhamChiTiet> findAllSanPham();

}
