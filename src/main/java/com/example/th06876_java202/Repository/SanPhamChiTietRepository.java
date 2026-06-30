package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPhamChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query(value = "update SanPhamChiTiet set TrangThai = :trangThai, NgayCapNhat = GETDATE() where MaSanPhamChiTiet = :id", nativeQuery = true)
    int updateTrangThaii(@Param("id") int id, @Param("trangThai") String trangThai);


    @Modifying
    @Transactional
    @Query(value = "update SanPhamChiTiet set TrangThai = case when SoLuongTon = 0 then N'Hết hàng' when SoLuongTon < 10 then N'Sắp hết' else N'Còn hàng' end, NgayCapNhat = GETDATE() where MaSanPham = ?", nativeQuery = true)
    int updateTrangThaiii(int maSanPham);


    List<SanPhamChiTiet> findBySanPham_MaSanPhamAndMauSac_TenMauSac(
            Integer maSP,
            String tenMauSac);

    Page<SanPhamChiTiet> findByMauSac_MaMauSac(String maMauSac, Pageable pageable);

    Page<SanPhamChiTiet> findByKichThuoc_MaKichThuoc(String maKichThuoc, Pageable pageable);

    @Query( value = "select distinct ms.TenMauSac from SanPhamChiTiet spct inner join MauSac ms on spct.MaMauSac = ms.MaMauSac order by ms.TenMauSac", nativeQuery = true)
    List<String> findAllMauSac();

    @Query( value = "select distinct kt.TenKichThuoc from SanPhamChiTiet spct inner join KichThuoc kt on spct.MaKichThuoc = kt.MaKichThuoc order by kt.TenKichThuoc", nativeQuery = true)
    List<String> findAllSize();

    @Query("SELECT s FROM SanPhamChiTiet s WHERE s.trangThai = :tt")
    Page<SanPhamChiTiet> locTheoTrangThaiHienThi(@Param("tt") String tt, Pageable pageable);

    @Query(value = "select * from SanPhamChiTiet where GiaBan >= ?1 AND GiaBan <= ?2",
            countQuery = "select count(*) from SanPhamChiTiet where GiaBan >= ?1 AND GiaBan <= ?2",
            nativeQuery = true)
    Page<SanPhamChiTiet> findByGiaBanAndGiaBan(BigDecimal gt, BigDecimal gb, Pageable pageable);

    @Query("SELECT MAX(s.giaBan) FROM SanPhamChiTiet s")
    Double findMaxGiaBan();

    @Query( value = " SELECT Sum(SoLuongTon) FROM SanPhamChiTiet ", nativeQuery = true)
    Integer sluong();


    @Query( value = "select * from SanPhamChiTiet where MaMauSac = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByMauSac(String mauSac);

    @Query( value = "select * from SanPhamChiTiet where MaKichThuoc = ?", nativeQuery = true)
    List<SanPhamChiTiet> findBySize(String size);

    @Query( value = "select * from SanPhamChiTiet where TrangThai = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByTT(String tt);

    @Query( value = "select * from SanPhamChiTiet where MaSanPham = ?", nativeQuery = true)
    List<SanPhamChiTiet> findByMaSanPham(Integer maSanPham);

    @Query(value = "SELECT * FROM SanPhamChiTiet WHERE MaSanPham IN (:listMaSanPham)", nativeQuery = true)
    List<SanPhamChiTiet> findByidmasp(@Param("listMaSanPham") List<Integer> listMaSanPham);
}
