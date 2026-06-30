package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    boolean existsByTenSanPham(String TenSanPham);

    @Modifying
    @Transactional
    @Query(value = "UPDATE SanPham SET TrangThai = ?2, NgayCapNhat = GETDATE() WHERE MaSanPham = ?1", nativeQuery = true)
    int updateTrangThai(int maSanPham, boolean trangThai);

    @Query(value = "Select * from SanPham where MaDanhMuc = ?", nativeQuery = true)
    List<SanPham> getallbymaDanhMuc(int maDanhMuc);

    @Query(value = "select * from SanPham where TenSanPham like CONCAT('%', :keyword, '%') or CAST(MaSanPham AS VARCHAR(20)) LIKE CONCAT('%', :keyword, '%')",
            nativeQuery = true)
    List<SanPham> timkiem(@Param("keyword") String keyword);

    @Query( value = "select * from SanPham where TrangThai = ?", nativeQuery = true)
    List<SanPham> getallbyTrangThai(String tt);

    boolean existsByTenSanPhamIgnoreCase(String tenSanPham);

    Page<SanPham> findAllByOrderByMaSanPhamDesc(Pageable pageable);

    List<SanPham> findTop8ByTrangThaiTrueOrderByMaSanPhamDesc();

    @Query("SELECT s FROM SanPham s WHERE " +
            "(:maDanhMuc IS NULL OR s.danhMucSanPham.maDanhMuc = :maDanhMuc) AND " +
            "(:tt IS NULL OR s.trangThai = :tt) AND " +
            "(:maTH IS NULL OR s.thuongHieu.maThuongHieu = :maTH) AND " +
            "(:maKG IS NULL OR s.kieuGiay.maKieuGiay = :maKG) AND " +
            "(:t IS NULL OR s.tenSanPham LIKE %:t% OR CAST(s.maSanPham AS string) LIKE %:t%) " +
            "ORDER BY s.maSanPham DESC")
    Page<SanPham> searchSanPham(@Param("maDanhMuc") Integer maDanhMuc,
                                @Param("tt") Boolean tt,
                                @Param("maTH") Integer maTH,
                                @Param("maKG") Integer maKG,
                                @Param("t") String t,
                                Pageable pageable);


}
