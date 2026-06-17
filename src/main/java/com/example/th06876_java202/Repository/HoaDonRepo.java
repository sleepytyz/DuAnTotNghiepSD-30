package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HoaDonRepo extends JpaRepository<HoaDon, Integer> {


    // Hoá đơn

    @Query( value = "select * from HoaDon where MaHoaDon = ? and TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')", nativeQuery = true)
    List<HoaDon> searchByMa(Integer maHoaDon);

    @Query( value = "select * from HoaDon where TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')", nativeQuery = true)
    List<HoaDon> getallHD();

    List<HoaDon> findByTrangThai(String trangThai);

    @Query(value = "select * from HoaDon where NgayTao  >= ? and  NgayTao <= ? and TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')", nativeQuery = true)
    List<HoaDon> findByNgayTao(LocalDate ngayTao1, LocalDate ngayTao2);


    //ĐƠn hàng

    @Query(value = "select * from HoaDon where NgayTao   >= ? and  NgayTao <= ? and TrangThai in (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao')", nativeQuery = true)
    List<HoaDon> findByNgayTaodh(
            LocalDate ngayTao1,
            LocalDate ngayTao2
    );

    @Query(value = "select * from HoaDon where TrangThai = N'Yêu cầu huỷ'", nativeQuery = true)
    List<HoaDon> findByTrangThai();

    @Query( value = "select * from HoaDon where MaHoaDon = ? and TrangThai in (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao')", nativeQuery = true)
    List<HoaDon> searchByMadh(Integer maHoaDon);

    @Query( value = "select * from HoaDon where TrangThai in (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao')", nativeQuery = true)
    List<HoaDon> getaddDH();

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đã xác nhận' where MaHoaDon = ?", nativeQuery = true)
    int suatt(Integer mahd);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đang giao' where MaHoaDon = ?", nativeQuery = true)
    int suattdg(Integer mahd);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đã giao' where MaHoaDon = ?", nativeQuery = true)
    int suattdgg(Integer mahd);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đã huỷ' where MaHoaDon = ?", nativeQuery = true)
    int huy(Integer mahd);


}
