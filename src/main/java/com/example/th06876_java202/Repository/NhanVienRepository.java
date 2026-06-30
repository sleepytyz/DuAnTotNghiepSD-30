package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "Update NhanVien set TrangThai = N'Đã nghỉ việc' where MaNhanVien = ?", nativeQuery = true)
    int updateTrangThai(Integer maNhanVien);

    @Query(value = "SELECT * FROM NhanVien WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR HoTen LIKE %:keyword% OR SoDienThoai LIKE %:keyword%) AND " +
            "(:role IS NULL OR :role = '' OR ChucVu = :role) AND " +
            "(:status IS NULL OR :status = '' OR CAST(TrangThai AS VARCHAR(5)) = :status)",
            countQuery = "SELECT count(*) FROM NhanVien WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR HoTen LIKE %:keyword% OR SoDienThoai LIKE %:keyword%) AND " +
                    "(:role IS NULL OR :role = '' OR ChucVu = :role) AND " +
                    "(:status IS NULL OR :status = '' OR CAST(TrangThai AS VARCHAR(5)) = :status)",
            nativeQuery = true)
    Page<NhanVien> filter(@Param("keyword") String keyword,
                          @Param("role") String role,
                          @Param("status") String status,
                          Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmailAndMaNhanVienNot(String email, Integer maNhanVien);

    boolean existsBySoDienThoaiAndMaNhanVienNot(String soDienThoai, Integer maNhanVien);

    List<NhanVien> findByHoTenContaining(String hoTen);

    NhanVien findByTaiKhoan_TenDangNhap(String tenDangNhap);
}