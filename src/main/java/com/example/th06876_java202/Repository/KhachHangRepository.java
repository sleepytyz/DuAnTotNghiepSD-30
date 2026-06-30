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

public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    @Query("select k from KhachHang k where k.sdt like concat('%', :sdt, '%')")
    Page<KhachHang> findBySdtt(@Param("sdt") String sdt, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = " INSERT INTO KhachHang(MaKhachHang, HoTen, SoDienThoai, TrangThai) VALUES (?1, ?2, ?3, 1)", nativeQuery = true)
    int saveee(String maKH, String hoTen, String soDienThoai);

    List<KhachHang> findTop10BySdtContaining(String sdt);



    // Method cũ - có phân trang (giữ nguyên)
    @Query("SELECT k FROM KhachHang k WHERE k.sdt LIKE CONCAT('%', :sdt, '%')")
    Page<KhachHang> findBySdtContaining(@Param("sdt") String sdt, Pageable pageable);

    // ===== THÊM METHOD MỚI - LẤY TẤT CẢ (KHÔNG PHÂN TRANG) =====
    @Query("SELECT k FROM KhachHang k WHERE (:sdt IS NULL OR :sdt = '' OR k.sdt LIKE CONCAT('%', :sdt, '%'))")
    List<KhachHang> findAllBySdt(@Param("sdt") String sdt);



    @Modifying
    @Transactional
    @Query("update KhachHang k set k.trangThai = false where k.maKH = :maKH")
    void updateTrangThai(@Param("maKH") String maKH);

    @Modifying
    @Transactional
    @Query("update KhachHang k set k.trangThai = true where k.maKH = :maKH")
    void restoreTrangThai(@Param("maKH") String maKH);

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByTaiKhoan_TenDangNhap(String tenDangNhap);

    boolean existsBySdt(String sdt);
    boolean existsByEmail(String email);

    boolean existsBySdtAndMaKHNot(String sdt, String maKH);
    boolean existsByEmailAndMaKHNot(String email, String maKH);

    @Query("SELECT COUNT(dc) > 0 FROM DiaChi dc WHERE dc.soDienThoaiNguoiNhan = :sdt AND dc.khachHang.maKH != :maKH")
    boolean existsBySdtInDiaChi(@Param("sdt") String sdt, @Param("maKH") String maKH);

    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = :trangThai")
    Page<KhachHang> findByTrangThai(@Param("trangThai") boolean trangThai, Pageable pageable);

    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = :trangThai")
    List<KhachHang> findAllByTrangThai(@Param("trangThai") boolean trangThai);




    List<KhachHang> findByTrangThai(Boolean trangThai);

    List<KhachHang> findBySdtContaining(String sdt);

    // Tìm theo nhiều điều kiện (nếu cần)
    @Query("SELECT k FROM KhachHang k WHERE " +
            "(:sdt IS NULL OR k.sdt LIKE %:sdt%) AND " +
            "(:status IS NULL OR k.trangThai = :status)")
    List<KhachHang> findByFilters(@Param("sdt") String sdt,
                                  @Param("status") Boolean status);

}