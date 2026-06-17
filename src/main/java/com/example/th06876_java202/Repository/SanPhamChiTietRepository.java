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

    @Query( value = "select * from SanPhamChiTiet where MaMauSac = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByMauSac(String mauSac);

    @Query( value = "select * from SanPhamChiTiet where MaKichThuoc = ?", nativeQuery = true)
    List<SanPhamChiTiet> findBySize(String size);

    @Query( value = "select distinct ms.TenMauSac from SanPhamChiTiet spct inner join MauSac ms on spct.MaMauSac = ms.MaMauSac order by ms.TenMauSac", nativeQuery = true)
    List<String> findAllMauSac();

    @Query( value = "select distinct kt.TenKichThuoc from SanPhamChiTiet spct inner join KichThuoc kt on spct.MaKichThuoc = kt.MaKichThuoc order by kt.TenKichThuoc", nativeQuery = true)
    List<String> findAllSize();

    @Query( value = "select * from SanPhamChiTiet where TrangThai = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByTrangThai(String tt);

    @Query(value = "select * from SanPhamChiTiet  where GiaBan >= ? and GiaBan <= ?", nativeQuery = true)
    List<SanPhamChiTiet> findByGiaBanAndGiaBan(BigDecimal gt, BigDecimal gb);
}
