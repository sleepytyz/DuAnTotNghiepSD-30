package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    @Query("select k from KhachHang k where k.sdt like concat('%', :sdt, '%')")
    List<KhachHang> findBySdt(@Param("sdt") String sdt);


    @Query( value = "select * from KhachHang where HangKhachHang = ?", nativeQuery = true)
    List<KhachHang> findByHangKhachHang(String hang);

    @Modifying
    @Transactional
    @Query(value = "update KhachHang set TrangThai = 0 where MaKhachHang = ?", nativeQuery = true)
    void updateTrangThai(Integer mkh);

    boolean existsBySdt(String sdt);

    @Modifying
    @Transactional
    @Query(value = " INSERT INTO KhachHang(HoTen, SoDienThoai, DiaChi, TrangThai, HangKhachHang) VALUES (?1, ?2, ?3, 1, N'Mới')", nativeQuery = true)
    int saveee(String hoTen, String soDienThoai, String diaChi);



}
