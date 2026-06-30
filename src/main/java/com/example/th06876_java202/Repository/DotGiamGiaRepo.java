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

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DotGiamGiaRepo extends JpaRepository<DotGiamGia, Integer> {

    @Query(value = "select dgg.* from DotGiamGia dgg inner join ChiTietDotGiamGia ctdgg on dgg.MaGiamGia = ctdgg.MaGiamGia where ctdgg.MaSanPham = ? and dgg.TrangThai = 1", nativeQuery = true)
    List<DotGiamGia> findBySanPham(Integer maSanPham);

    @Modifying
    @Transactional
    @Query(value = "update DotGiamGia set TrangThai = N'Ngừng hoạt động' where MaGiamGia = ?", nativeQuery = true)
    void updateTrangThai(Integer maSanPham);

    @Query(value = """
SELECT * FROM DotGiamGia
WHERE
(:keyword IS NULL OR :keyword = '' OR
 TenGiamGia LIKE CONCAT('%', :keyword, '%')
 OR MaGiamGia LIKE CONCAT('%', :keyword, '%'))

AND (:trangThai IS NULL OR :trangThai = '' OR TrangThai = :trangThai)

AND (:tuNgay IS NULL OR NgayBatDau >= :tuNgay)

AND (:denNgay IS NULL OR NgayKetThuc <= :denNgay)
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
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            Pageable pageable
    );

    Page<DotGiamGia> findAllByOrderByMaGiamGiaDesc(Pageable pageable);

}
