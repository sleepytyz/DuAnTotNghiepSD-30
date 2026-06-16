package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    List<KhachHang> findBySdtContains(String sdt);

    List<KhachHang> findByHangKhachHang(String hang);

    @Modifying
    @Transactional
    @Query("update KhachHang k set k.trangThai = false where k.maKH = :maKH")
    void updateTrangThai(@Param("maKH") Integer mkh);

    @Modifying
    @Transactional
    @Query("update KhachHang k set k.trangThai = true where k.maKH = :maKH")
    void restoreTrangThai(@Param("maKH") Integer mkh);

    boolean existsBySdt(String sdt);

    Optional<KhachHang> findByEmail(String email);
}
