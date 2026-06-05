package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    boolean existsByTenSanPham(String TenSanPham);

    @Modifying
    @Transactional
    @Query(value = "UPDATE SanPham SET TrangThai = N'0' WHERE MaSanPham = ?1",
            nativeQuery = true)
    int updateTrangThaiNgungBan(int maSanPham);
}
