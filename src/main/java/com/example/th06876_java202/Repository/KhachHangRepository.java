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
    List<KhachHang> findBySdt(@Param("sdt") String sdt);

    List<KhachHang> findByHangKhachHang(String sdt);

    @Modifying
    @Transactional
    @Query(value = " INSERT INTO KhachHang(HoTen, SoDienThoai, DiaChi, TrangThai, HangKhachHang) VALUES (?1, ?2, ?3, 1, N'Mới')", nativeQuery = true)
    int saveee(String hoTen, String soDienThoai, String diaChi);


    Page<KhachHang> findBySdtContaining(String sdt, Pageable pageable);
    Page<KhachHang> findByHangKhachHang(String hang, Pageable pageable);



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
