package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonRepo extends JpaRepository<HoaDon, String> {

    // ===== PHƯƠNG THỨC MỚI =====

    // Phân trang - Tìm theo danh sách trạng thái
    Page<HoaDon> findByTrangThaiIn(List<String> trangThaiList, Pageable pageable);

    // Không phân trang - Tìm theo danh sách trạng thái
    List<HoaDon> findByTrangThaiIn(List<String> trangThaiList);

    // Phân trang - Tìm theo khoảng ngày và danh sách trạng thái
    Page<HoaDon> findByNgayTaoBetweenAndTrangThaiIn(LocalDateTime tuNgay, LocalDateTime denNgay,
                                                    List<String> trangThaiList, Pageable pageable);

    // Không phân trang - Tìm theo khoảng ngày và danh sách trạng thái
    List<HoaDon> findByNgayTaoBetweenAndTrangThaiIn(LocalDateTime tuNgay, LocalDateTime denNgay,
                                                    List<String> trangThaiList);

    // Phân trang - Tìm theo ngày sau và danh sách trạng thái
    Page<HoaDon> findByNgayTaoAfterAndTrangThaiIn(LocalDateTime ngay, List<String> trangThaiList, Pageable pageable);

    // Không phân trang - Tìm theo ngày sau và danh sách trạng thái
    List<HoaDon> findByNgayTaoAfterAndTrangThaiIn(LocalDateTime ngay, List<String> trangThaiList);

    // Phân trang - Tìm theo ngày trước và danh sách trạng thái
    Page<HoaDon> findByNgayTaoBeforeAndTrangThaiIn(LocalDateTime ngay, List<String> trangThaiList, Pageable pageable);

    // Không phân trang - Tìm theo ngày trước và danh sách trạng thái
    List<HoaDon> findByNgayTaoBeforeAndTrangThaiIn(LocalDateTime ngay, List<String> trangThaiList);

    // Phân trang - Tìm theo mã và danh sách trạng thái
    Page<HoaDon> findByMaHoaDonAndTrangThaiIn(String maHoaDon, List<String> trangThaiList, Pageable pageable);

    // ===== PHƯƠNG THỨC CŨ =====

    Page<HoaDon> findByNgayTaoBetween(LocalDateTime tuNgay, LocalDateTime denNgay, Pageable pageable);

    List<HoaDon> findByNgayTaoBetween(LocalDateTime tuNgay, LocalDateTime denNgay);

    Page<HoaDon> findByNgayTaoAfter(LocalDateTime ngay, Pageable pageable);

    List<HoaDon> findByNgayTaoAfter(LocalDateTime ngay);

    Page<HoaDon> findByNgayTaoBefore(LocalDateTime ngay, Pageable pageable);

    List<HoaDon> findByNgayTaoBefore(LocalDateTime ngay);

    Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable);

    List<HoaDon> findByTrangThai(String trangThai);

    long countByTrangThai(String trangThai);

    @Query("SELECT h FROM HoaDon h WHERE h.trangThai NOT IN :ds")
    Page<HoaDon> findByTrangThaiNotIn(@Param("ds") List<String> ds, Pageable pageable);

    Page<HoaDon> findAll(Pageable pageable);

    @Query(value = "select * from HoaDon where MaHoaDon = ?1 AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')",
            countQuery = "select count(*) from HoaDon where MaHoaDon = ?1 AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')",
            nativeQuery = true)
    Page<HoaDon> searchByMa(Integer maHoaDon, Pageable pageable);

    @Query(value = "select * from HoaDon where TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng') order by MaHoaDon desc",
            nativeQuery = true)
    List<HoaDon> getallHD();

    @Query(value = "select * from HoaDon where TrangThai = ?1",
            countQuery = "select count(*) from HoaDon where TrangThai = ?1",
            nativeQuery = true)
    Page<HoaDon> findByTrangThaii(String trangThai, Pageable pageable);

    @Query(
            value = "SELECT * FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 " +
                    "AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng') " +
                    "ORDER BY MaHoaDon DESC",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 " +
                    "AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')",
            nativeQuery = true)
    Page<HoaDon> findByNgayTao(LocalDateTime ngayTao1, LocalDateTime ngayTao2, Pageable pageable);

    @Query(value = "select * from HoaDon where TrangThai = N'Yêu cầu huỷ'", nativeQuery = true)
    List<HoaDon> findByTrangThai();

    @Query(value = "SELECT * FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            nativeQuery = true)
    Page<HoaDon> findByNgayTaodh(LocalDateTime ngayTao1, LocalDateTime ngayTao2, Pageable pageable);

    @Query(value = "SELECT * FROM HoaDon WHERE MaHoaDon = ?1 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE MaHoaDon = ?1 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            nativeQuery = true)
    Page<HoaDon> searchByMadh(Integer maHoaDon, Pageable pageable);

    @Query(value = "SELECT * FROM HoaDon WHERE TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao' , N'Đang xử lý')",
            nativeQuery = true)
    Page<HoaDon> getaddDH(Pageable pageable);

    // [SỬA] Trước đây chỉ tính TrangThai = N'Đã thanh toán' (chỉ đơn bán tại quầy),
    // nên toàn bộ đơn bán online đã giao thành công (TrangThai = N'Đã giao') KHÔNG được
    // tính vào doanh thu. Nay tính cả 2 trạng thái coi như "đã thu tiền thành công".
    @Query(value = """
            SELECT 
                CAST(h.NgayTao AS DATE) as ngay,
                COUNT(h.MaHoaDon) as soDonHang,
                ISNULL(SUM(h.TongTien), 0) as doanhThu,
                ISNULL(AVG(h.TongTien), 0) as trungBinhDon
            FROM HoaDon h
            WHERE h.TrangThai IN (N'Đã thanh toán', N'Đã giao')
                AND h.NgayTao BETWEEN ?1 AND ?2
            GROUP BY CAST(h.NgayTao AS DATE)
            ORDER BY CAST(h.NgayTao AS DATE) DESC
            """, nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoNgay(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = """
            SELECT 
                YEAR(h.NgayTao) as nam,
                MONTH(h.NgayTao) as thang,
                COUNT(h.MaHoaDon) as soDonHang,
                ISNULL(SUM(h.TongTien), 0) as doanhThu
            FROM HoaDon h
            WHERE h.TrangThai IN (N'Đã thanh toán', N'Đã giao')
                AND h.NgayTao BETWEEN ?1 AND ?2
            GROUP BY YEAR(h.NgayTao), MONTH(h.NgayTao)
            ORDER BY YEAR(h.NgayTao) DESC, MONTH(h.NgayTao) DESC
            """, nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoThang(LocalDateTime startDate, LocalDateTime endDate);

    // [SỬA] Trước đây hàm này KHÔNG nhận tham số ngày -> 4 ô số liệu tổng quan trên
    // trang thống kê luôn hiển thị TOÀN BỘ lịch sử, không khớp với khoảng ngày mà
    // người dùng chọn lọc bên dưới (đây chính là nguyên nhân số liệu "không đúng").
    // Nay lọc đúng theo khoảng ngày được chọn, đồng thời tính cả trạng thái 'Đã giao'.
    @Query(value = """
            SELECT 
                ISNULL(COUNT(h.MaHoaDon), 0) as tongDonHang,
                ISNULL(SUM(h.TongTien), 0) as tongDoanhThu,
                ISNULL(AVG(h.TongTien), 0) as trungBinhDon,
                MIN(h.NgayTao) as ngayDau,
                MAX(h.NgayTao) as ngayCuoi
            FROM HoaDon h
            WHERE h.TrangThai IN (N'Đã thanh toán', N'Đã giao')
                AND h.NgayTao BETWEEN ?1 AND ?2
            """, nativeQuery = true)
    List<Object[]> thongKeTongQuan(LocalDateTime startDate, LocalDateTime endDate);

    // [MỚI] Hiệu suất bán hàng theo từng nhân viên trong khoảng ngày (dùng cho
    // trang thống kê của ADMIN). Chỉ tính đơn có gán nhân viên (bán tại quầy).
    @Query(value = """
            SELECT 
                h.MaNhanVien as maNhanVien,
                nv.HoTen as hoTen,
                COUNT(h.MaHoaDon) as soDonHang,
                ISNULL(SUM(h.TongTien), 0) as doanhThu
            FROM HoaDon h
            JOIN NhanVien nv ON nv.MaNhanVien = h.MaNhanVien
            WHERE h.TrangThai IN (N'Đã thanh toán', N'Đã giao')
                AND h.NgayTao BETWEEN ?1 AND ?2
                AND h.MaNhanVien IS NOT NULL
            GROUP BY h.MaNhanVien, nv.HoTen
            ORDER BY doanhThu DESC
            """, nativeQuery = true)
    List<Object[]> thongKeHieuSuatBanHangTheoNhanVien(LocalDateTime startDate, LocalDateTime endDate);

    // [MỚI] Doanh thu theo ngày CỦA RIÊNG 1 nhân viên (dùng cho trang "Thống kê của tôi" - STAFF)
    @Query(value = """
            SELECT 
                CAST(h.NgayTao AS DATE) as ngay,
                COUNT(h.MaHoaDon) as soDonHang,
                ISNULL(SUM(h.TongTien), 0) as doanhThu,
                ISNULL(AVG(h.TongTien), 0) as trungBinhDon
            FROM HoaDon h
            WHERE h.TrangThai IN (N'Đã thanh toán', N'Đã giao')
                AND h.MaNhanVien = ?1
                AND h.NgayTao BETWEEN ?2 AND ?3
            GROUP BY CAST(h.NgayTao AS DATE)
            ORDER BY CAST(h.NgayTao AS DATE) DESC
            """, nativeQuery = true)
    List<Object[]> thongKeDoanhThuCaNhanTheoNgay(String maNhanVien, LocalDateTime startDate, LocalDateTime endDate);

    // [MỚI] Tổng quan doanh số CỦA RIÊNG 1 nhân viên trong khoảng ngày (STAFF)
    @Query(value = """
            SELECT 
                ISNULL(COUNT(h.MaHoaDon), 0) as tongDonHang,
                ISNULL(SUM(h.TongTien), 0) as tongDoanhThu,
                ISNULL(AVG(h.TongTien), 0) as trungBinhDon,
                MIN(h.NgayTao) as ngayDau,
                MAX(h.NgayTao) as ngayCuoi
            FROM HoaDon h
            WHERE h.TrangThai IN (N'Đã thanh toán', N'Đã giao')
                AND h.MaNhanVien = ?1
                AND h.NgayTao BETWEEN ?2 AND ?3
            """, nativeQuery = true)
    List<Object[]> thongKeTongQuanCaNhan(String maNhanVien, LocalDateTime startDate, LocalDateTime endDate);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đã xác nhận' where MaHoaDon = ?", nativeQuery = true)
    int suatt(String mahd);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đang giao' where MaHoaDon = ?", nativeQuery = true)
    int suattdg(String mahd);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đã giao' where MaHoaDon = ?", nativeQuery = true)
    int suattdgg(String mahd);

    @Modifying
    @Transactional
    @Query(value = "update HoaDon set TrangThai = N'Đã huỷ' where MaHoaDon = ?", nativeQuery = true)
    int huy(String mahd);

    List<HoaDon> findByTrangThaiAndLoaiBan(String trangThai, String loaiBan);

    Page<HoaDon> findByMaKhachHang_MaKHOrderByMaHoaDonDesc(String maKH, Pageable pageable);
}