package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    @Query("select k from KhachHang k where k.sdt like concat('%', :sdt, '%')")
    List<KhachHang> findBySdtt(@Param("sdt") String sdt);

    @Modifying
    @Transactional
    @Query(value = " INSERT INTO KhachHang(HoTen, SoDienThoai, DiaChi, TrangThai, HangKhachHang) VALUES (?1, ?2, ?3, 1, N'Mới')", nativeQuery = true)
    int saveee(String hoTen, String soDienThoai, String diaChi);

    List<KhachHang> findTop10BySdtContaining(String sdt);

    Page<KhachHang> findBySdtContaining(String sdt, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update KhachHang k set k.trangThai = false where k.maKH = :maKH")
    void updateTrangThai(@Param("maKH") Integer mkh);

    @Modifying
    @Transactional
    @Query("update KhachHang k set k.trangThai = true where k.maKH = :maKH")
    void restoreTrangThai(@Param("maKH") Integer mkh);

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByTaiKhoan_TenDangNhap(String tenDangNhap);

    boolean existsBySdt(String sdt);
    boolean existsByEmail(String email);

    boolean existsBySdtAndMaKHNot(String sdt, Integer maKH);
    boolean existsByEmailAndMaKHNot(String email, Integer maKH);



}
