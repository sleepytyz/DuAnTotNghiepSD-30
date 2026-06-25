package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {

    @Query(value = "select * from GiamGia where SoLuong > 1", nativeQuery = true)
    List<GiamGia> findSoLuongVoucher();

    @Modifying
    @Transactional
    @Query("UPDATE GiamGia g SET g.soLuong = g.soLuong - 1 WHERE g.maGiamGia = :id AND g.soLuong > 0")
    int giamSoLuongVoucher(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update GiamGia set TrangThai = N'Ngừng hoạt động' where MaGiamGia = ?", nativeQuery = true)
    void updateGiamGiaaa(int id);


    @Query(value = "select * from GiamGia where LoaiGiamGia = ?", nativeQuery = true)
    List<GiamGia> getGiamGia(String loaiGia);

    @Query(value = "select * from GiamGia where TrangThai = ?", nativeQuery = true)
    List<GiamGia> loctt(String tt);

    @Query(value = "select * from GiamGia where TenChuongTrinh like CONCAT('%', :keyword , '%' ) or CAST(MaGiamGia as Varchar(30)) like CONCAT('%', :keyword, '%' ) ", nativeQuery = true)
    List<GiamGia> timkiem(@Param("keyword") String keyword);

    @Query(value = "select * from GiamGia where NgayBatDau > ? and NgayKetThuc < ?", nativeQuery = true)
    List<GiamGia> timkiemngay(LocalDateTime ngaybd, LocalDateTime ngayketthuc);

    boolean existsByTenGiamGia(String tenChuongTrinh);

    @Query("SELECT g FROM GiamGia g WHERE g.trangThai != 'Ngừng hoạt động'")
    List<GiamGia> findDanhSachCanCapNhat();

    @Modifying
    @Transactional
    @Query("UPDATE GiamGia g SET g.trangThai = :trangThai WHERE g.maGiamGia = :id")
    void updateTrangThai(@Param("trangThai") String trangThai, @Param("id") int id);

    @Query(value = """
        SELECT * FROM GiamGia 
        WHERE (:keyword IS NULL OR :keyword = '' OR TenChuongTrinh LIKE %:keyword% OR CAST(MaGiamGia AS VARCHAR(30)) LIKE %:keyword%)
        AND (:tt IS NULL OR :tt = '' OR TrangThai = :tt)
        AND (:lg IS NULL OR :lg = '' OR LoaiGiamGia = :lg)
        AND (:tuNgay IS NULL OR NgayBatDau >= :tuNgay)
        AND (:denNgay IS NULL OR NgayKetThuc <= :denNgay)
        ORDER BY MaGiamGia DESC
    """, nativeQuery = true, countQuery = "SELECT count(*) FROM GiamGia")
    Page<GiamGia> filterAll(
            @Param("keyword") String keyword,
            @Param("tt") String tt,
            @Param("lg") String lg,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable
    );




}
