package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DiaChi;
import com.example.th06876_java202.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiRepo extends JpaRepository<DiaChi, Integer> {

    List<DiaChi> findByKhachHang_MaKH(String maKH);

    // Thêm phương thức tìm địa chỉ mặc định
    @Query("SELECT d FROM DiaChi d WHERE d.khachHang.maKH = :maKH AND d.diaChiMacDinh = true")
    DiaChi findDefaultByMaKH(@Param("maKH") String maKH);

    // ============ TÌM THEO ĐỐI TƯỢNG KHÁCH HÀNG ============
    List<DiaChi> findByKhachHang(KhachHang khachHang);

    // ============ TÌM ĐỊA CHỈ MẶC ĐỊNH CỦA KHÁCH HÀNG ============
    DiaChi findByKhachHangAndDiaChiMacDinh(KhachHang khachHang, Boolean macDinh);

    // ============ ĐẾM SỐ LƯỢNG ĐỊA CHỈ CỦA KHÁCH HÀNG ============
    long countByKhachHang_MaKH(String maKH);

    // ============ TÌM THEO SỐ ĐIỆN THOẠI (LIKE) ============
    List<DiaChi> findBySoDienThoaiNguoiNhanContaining(String soDienThoai);

    // ============ TÌM THEO TÊN NGƯỜI NHẬN (LIKE) ============
    List<DiaChi> findByTenNguoiNhanContaining(String tenNguoiNhan);

    // ============ XÓA TẤT CẢ ĐỊA CHỈ CỦA KHÁCH HÀNG ============
    @Modifying
    @Transactional
    @Query("DELETE FROM DiaChi d WHERE d.khachHang.maKH = :maKH")
    void deleteByKhachHang_MaKH(@Param("maKH") String maKH);


    // ============ RESET TẤT CẢ ĐỊA CHỈ MẶC ĐỊNH ============
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.diaChiMacDinh = false WHERE d.khachHang.maKH = :maKH")
    void resetAllDefault(@Param("maKH") String maKH);

    // ============ TÌM THEO TỈNH/THÀNH ============
    List<DiaChi> findByTinhThanh(String tinhThanh);

    // ============ TÌM THEO QUẬN/HUYỆN ============
    List<DiaChi> findByQuanHuyen(String quanHuyen);

    // ============ TÌM ĐỊA CHỈ THEO ID VÀ MÃ KHÁCH HÀNG ============
    Optional<DiaChi> findByMaDiaChiAndKhachHang_MaKH(Integer maDiaChi, String maKH);
}