package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DotGiamGia;
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
public interface DotGiamGiaRepo extends JpaRepository<DotGiamGia, String> {

    // ===== SỬA: So sánh với chuỗi 'Hoạt động' (nvarchar) =====
    @Query(value = """
        SELECT dgg.* FROM DotGiamGia dgg 
        INNER JOIN ChiTietDotGiamGia ctdgg ON dgg.MaGiamGia = ctdgg.MaGiamGia 
        WHERE ctdgg.MaSanPham = ?1 AND dgg.TrangThai = N'Hoạt động'
        """, nativeQuery = true)
    List<DotGiamGia> findBySanPham(String maSanPham);

    @Modifying
    @Transactional
    @Query(value = "UPDATE DotGiamGia SET TrangThai = N'Ngừng hoạt động' WHERE MaGiamGia = ?1", nativeQuery = true)
    void updateTrangThai(String maSanPham);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE DotGiamGia SET TrangThai = N'Đã huỷ' WHERE MaGiamGia = :id AND TrangThai = N'Sắp hoạt động'", nativeQuery = true)
    int cancelVoucher(@Param("id") String id);

    // ===== FILTER PAGING =====
    @Query(value = """
        SELECT * FROM DotGiamGia
        WHERE
        (:keyword IS NULL OR :keyword = '' OR
         TenGiamGia LIKE CONCAT('%', :keyword, '%')
         OR MaGiamGia LIKE CONCAT('%', :keyword, '%'))
        AND (:trangThai IS NULL OR :trangThai = '' OR TrangThai = :trangThai)
        AND (:tuNgay IS NULL OR NgayBatDau >= :tuNgay)
        AND (:denNgay IS NULL OR NgayKetThuc <= :denNgay)
        ORDER BY NgayTao DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM DotGiamGia
        WHERE
        (:keyword IS NULL OR :keyword = '' OR
         TenGiamGia LIKE CONCAT('%', :keyword, '%')
         OR MaGiamGia LIKE CONCAT('%', :keyword, '%'))
        AND (:trangThai IS NULL OR :trangThai = '' OR TrangThai = :trangThai)
        AND (:tuNgay IS NULL OR NgayBatDau >= :tuNgay)
        AND (:denNgay IS NULL OR NgayKetThuc <= :denNgay)
        """,
            nativeQuery = true)
    Page<DotGiamGia> filterPaging(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable
    );

    Page<DotGiamGia> findAllByOrderByMaGiamGiaDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE DotGiamGia SET NgayBatDau = GETDATE(), TrangThai = N'Hoạt động' WHERE MaGiamGia = :id", nativeQuery = true)
    void activateVoucher(@Param("id") String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE DotGiamGia SET TrangThai = N'Ngừng hoạt động' WHERE MaGiamGia = :id", nativeQuery = true)
    void updateTrangThaiToStop(@Param("id") String id);

    // ===== LẤY TẤT CẢ ĐANG HOẠT ĐỘNG =====
    @Query(value = "SELECT * FROM DotGiamGia WHERE TrangThai = N'Hoạt động'", nativeQuery = true)
    List<DotGiamGia> findAllByTrangThaiHoatDong();

    // ===== LẤY THEO SẢN PHẨM VỚI NHIỀU TRẠNG THÁI =====
    @Query(value = """
        SELECT dgg.* FROM DotGiamGia dgg 
        INNER JOIN ChiTietDotGiamGia ctdgg ON dgg.MaGiamGia = ctdgg.MaGiamGia 
        WHERE ctdgg.MaSanPham = ?1 
        AND dgg.TrangThai IN (N'Hoạt động', N'Sắp hoạt động')
        """, nativeQuery = true)
    List<DotGiamGia> findBySanPhamAndTrangThaiActive(String maSanPham);
}