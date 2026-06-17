package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Modifying
    @Transactional
    @Query(value = "update SanPhamChiTiet set TrangThai = N'Ngừng bán', NgayCapNhat = GETDATE() where MaSanPham = ?", nativeQuery = true)
    int updateTrangThai(int maSanPham);





    @Modifying
    @Transactional
    @Query(value = "update SanPhamChiTiet set TrangThai = case when SoLuongTon = 0 then N'Hết hàng' when SoLuongTon <= 10 then N'Sắp hết' else N'Còn hàng' end, NgayCapNhat = GETDATE() where MaSanPham = ?", nativeQuery = true)
    int updateTrangThaiii(int maSanPham);





    List<SanPhamChiTiet> findBySanPham_MaSanPhamAndMauSac(
            Integer maSP,
            String mauSac);

    @Query( value = "SELECT MIN(ct.maSanPhamChiTiet) FROM SanPhamChiTiet ct JOIN SanPhamHinhAnh ha ON ct.maSanPhamChiTiet = ha.sanPhamChiTiet.maSanPhamChiTiet WHERE ha.laAnhChinh = true GROUP BY ct.sanPham, ct.mauSac")
    List<SanPhamChiTiet> findAllSanPham();

    @Query( value = "select * from SanPhamChiTiet where MauSac = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByMauSac(String mauSac);

    @Query( value = "select * from SanPhamChiTiet where Size = ?", nativeQuery = true)
    List<SanPhamChiTiet> findBySize(String size);

    @Query( value = "select distinct MauSac from SanPhamChiTiet order by MauSac", nativeQuery = true)
    List<String> findAllMauSac();

    @Query( value = "select distinct Size from SanPhamChiTiet order by Size", nativeQuery = true)
    List<String> findAllSize();

    @Query( value = "select * from SanPhamChiTiet where TrangThai = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByTrangThai(String tt);

    @Query(value = "select * from SanPhamChiTiet  where GiaBan >= ? and GiaBan <= ?", nativeQuery = true)
    List<SanPhamChiTiet> findByGiaBanAndGiaBan(BigDecimal gt, BigDecimal gb);
}
