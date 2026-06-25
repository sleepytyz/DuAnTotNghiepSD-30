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

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HoaDonRepo extends JpaRepository<HoaDon, Integer> {


    // Hoá đơn

    @Query("SELECT h FROM HoaDon h WHERE h.trangThai NOT IN :ds")
    Page<HoaDon> findByTrangThaiNotIn(@Param("ds") List<String> ds, Pageable pageable);

    Page<HoaDon> findAll(Pageable pageable);

    @Query(value = "select * from HoaDon where MaHoaDon = ?1 AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')",
            countQuery = "select count(*) from HoaDon where MaHoaDon = ?1 AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')",
            nativeQuery = true)
    Page<HoaDon> searchByMa(Integer maHoaDon, Pageable pageable);

    @Query( value = "select * from HoaDon where TrangThai in (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng') order by MaHoaDon desc", nativeQuery = true)
    List<HoaDon> getallHD();

    @Query(value = "select * from HoaDon where TrangThai = ?1",
            countQuery = "select count(*) from HoaDon where TrangThai = ?1",
            nativeQuery = true)
    Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable);

    @Query(
            value = "SELECT * FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 " +
                    "AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng') " +
                    "ORDER BY MaHoaDon DESC",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 " +
                    "AND TrangThai IN (N'Đã thanh toán', N'Đã giao', N'Đã huỷ', N'Đã trả hàng')",
            nativeQuery = true
    )
    Page<HoaDon> findByNgayTao(LocalDate ngayTao1, LocalDate ngayTao2, Pageable pageable);


    //ĐƠn hàng


    @Query(value = "select * from HoaDon where TrangThai = N'Yêu cầu huỷ'", nativeQuery = true)
    List<HoaDon> findByTrangThai();

    @Query(value = "SELECT * FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE NgayTao >= ?1 AND NgayTao <= ?2 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            nativeQuery = true)
    Page<HoaDon> findByNgayTaodh(LocalDate ngayTao1, LocalDate ngayTao2, Pageable pageable);

    @Query(value = "SELECT * FROM HoaDon WHERE MaHoaDon = ?1 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE MaHoaDon = ?1 AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            nativeQuery = true)
    Page<HoaDon> searchByMadh(Integer maHoaDon, Pageable pageable);

    @Query(value = "SELECT * FROM HoaDon WHERE TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao', N'Đang xử lý')",
            countQuery = "SELECT COUNT(*) FROM HoaDon WHERE TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang giao' , N'Đang xử lý')",
            nativeQuery = true)
    Page<HoaDon> getaddDH(Pageable pageable);

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
