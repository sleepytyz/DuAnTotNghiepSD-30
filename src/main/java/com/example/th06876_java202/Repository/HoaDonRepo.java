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
    @Query(value = "select * from HoaDon where MaHoaDon = ? and TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')", nativeQuery = true)
    List<HoaDon> searchByMa(Integer maHoaDon);

    @Query(value = "select * from HoaDon where TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')", nativeQuery = true)
    List<HoaDon> getallHD();

    List<HoaDon> findByTrangThai(String trangThai);

    @Query(value = "select * from HoaDon where NgayTao  >= ? and  NgayTao <= ? and TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')", nativeQuery = true)
    List<HoaDon> findByNgayTao(LocalDate ngayTao1, LocalDate ngayTao2);

    //Đơn hàng
    @Query(value = "select * from HoaDon where NgayTao   >= ? and  NgayTao <= ? and TrangThai in (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao')", nativeQuery = true)
    List<HoaDon> findByNgayTaodh(LocalDate ngayTao1, LocalDate ngayTao2);

    @Query(value = "select * from HoaDon where TrangThai = N'Yêu cầu huỷ'", nativeQuery = true)
    List<HoaDon> findByTrangThai();

    @Query(value = "select * from HoaDon where MaHoaDon = ? and TrangThai in (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao')", nativeQuery = true)
    List<HoaDon> searchByMadh(Integer maHoaDon);

    @Query(value = "select * from HoaDon where TrangThai in (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao')", nativeQuery = true)
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

    // Thống kê doanh thu theo ngày - Sửa lại để trả về đúng kiểu
    @Query(value = """
    SELECT 
        CAST(h.NgayTao AS DATE) as ngay,
        COUNT(h.MaHoaDon) as soDonHang,
        ISNULL(SUM(h.TongTien), 0) as doanhThu,
        ISNULL(AVG(h.TongTien), 0) as trungBinhDon
    FROM HoaDon h
    WHERE h.TrangThai = N'Đã thanh toán'
        AND h.NgayTao BETWEEN :startDate AND :endDate
    GROUP BY CAST(h.NgayTao AS DATE)
    ORDER BY CAST(h.NgayTao AS DATE) DESC
""", nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoNgay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Thống kê doanh thu theo tháng
    @Query(value = """
        SELECT 
            YEAR(h.NgayTao) as nam,
            MONTH(h.NgayTao) as thang,
            COUNT(h.MaHoaDon) as soDonHang,
            ISNULL(SUM(h.TongTien), 0) as doanhThu
        FROM HoaDon h
        WHERE h.TrangThai = N'Đã thanh toán'
            AND h.NgayTao BETWEEN :startDate AND :endDate
        GROUP BY YEAR(h.NgayTao), MONTH(h.NgayTao)
        ORDER BY YEAR(h.NgayTao) DESC, MONTH(h.NgayTao) DESC
    """, nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoThang(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Tổng quan dashboard - Sửa lại để trả về List<Object[]>
    @Query(value = """
    SELECT 
        ISNULL(COUNT(h.MaHoaDon), 0) as tongDonHang,
        ISNULL(SUM(h.TongTien), 0) as tongDoanhThu,
        ISNULL(AVG(h.TongTien), 0) as trungBinhDon,
        MIN(h.NgayTao) as ngayDau,
        MAX(h.NgayTao) as ngayCuoi
    FROM HoaDon h
    WHERE h.TrangThai = N'Đã thanh toán'
""", nativeQuery = true)
    List<Object[]> thongKeTongQuan();  // Đổi từ Object[] sang List<Object[]>
}