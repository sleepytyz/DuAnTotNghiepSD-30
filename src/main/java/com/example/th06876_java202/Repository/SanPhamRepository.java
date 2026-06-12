package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
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
    @Query(value = "UPDATE SanPham SET TrangThai = N'0' WHERE MaSanPham = ?1",
            nativeQuery = true)
    int updateTrangThaiNgungBan(int maSanPham);

    @Query(value = "Select * from SanPham where MaDanhMuc = ?", nativeQuery = true)
    List<SanPham> getallbymaDanhMuc(int maDanhMuc);

    @Query(value = "select * from SanPham where TenSanPham like CONCAT('%', :keyword, '%') or CAST(MaSanPham AS VARCHAR(20)) LIKE CONCAT('%', :keyword, '%')",
            nativeQuery = true)
    List<SanPham> timkiem(@Param("keyword") String keyword);

    @Query( value = "select * from SanPham where TrangThai = ?", nativeQuery = true)
    List<SanPham> getallbyTrangThai(String tt);
}
