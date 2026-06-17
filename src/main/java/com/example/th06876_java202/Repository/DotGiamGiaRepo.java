package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DotGiamGiaRepo extends JpaRepository<DotGiamGia, Integer> {

    @Query(value = "select dgg.* from DotGiamGia dgg inner join ChiTietDotGiamGia ctdgg on dgg.MaGiamGia = ctdgg.MaGiamGia where ctdgg.MaSanPham = ? and dgg.TrangThai = 1", nativeQuery = true)
    List<DotGiamGia> findBySanPham(Integer maSanPham);

    @Modifying
    @Transactional
    @Query(value = "update DotGiamGia set TrangThai = 0 where MaGiamGia = ?", nativeQuery = true)
    void updateTrangThai(Integer maSanPham);

    @Query(value = "SELECT * FROM DotGiamGia WHERE " +
            "(:tenGiam IS NULL OR :tenGiam = '' OR TenGiamGia LIKE CONCAT('%', :tenGiam, '%') OR MaGiamGia LIKE CONCAT('%', :tenGiam, '%')) " +
            "AND (:trangThai IS NULL OR :trangThai = '' OR TrangThai = :trangThai) " +
            "AND (:loaiGiam IS NULL OR :loaiGiam = '' OR LoaiGiamGia = :loaiGiam)",
            nativeQuery = true)
    List<DotGiamGia> filterDotGiamGia(@Param("tenGiam") String tenGiam,
                                      @Param("trangThai") String trangThai,
                                      @Param("loaiGiam") String loaiGiam);

}
