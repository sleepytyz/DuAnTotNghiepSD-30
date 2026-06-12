package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE SanPhamChiTiet spct SET spct.trangThai = 'Ngừng bán' WHERE spct.maSanPhamChiTiet = :id")
    int updateTrangThaiNgungBan(@Param("id") int maSanPhamChiTiet);

    List<SanPhamChiTiet> findBySanPham_MaSanPhamAndMauSac(
            Integer maSP,
            String mauSac);

    @Query( value = "SELECT MIN(ct.maSanPhamChiTiet) FROM SanPhamChiTiet ct JOIN SanPhamHinhAnh ha ON ct.maSanPhamChiTiet = ha.sanPhamChiTiet.maSanPhamChiTiet WHERE ha.laAnhChinh = true GROUP BY ct.sanPham, ct.mauSac")
    List<SanPhamChiTiet> findAllSanPham();

}
