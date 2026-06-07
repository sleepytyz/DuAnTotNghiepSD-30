package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.SanPhamHinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamHinhAnhRepository
        extends JpaRepository<SanPhamHinhAnh, Integer> {

    List<SanPhamHinhAnh> findBySanPhamChiTiet_MaSanPhamChiTiet(Integer maSPCT);

    SanPhamHinhAnh findBySanPhamChiTiet_MaSanPhamChiTietAndLaAnhChinhTrue(Integer maSPCT);

    @Query("""
    SELECT a
    FROM SanPhamHinhAnh a
    WHERE a.MaHinhAnh IN (
        SELECT MIN(h.MaHinhAnh)
        FROM SanPhamHinhAnh h
        GROUP BY h.sanPhamChiTiet.maSanPham,
                 h.sanPhamChiTiet.mauSac
    )
""")
    List<SanPhamHinhAnh> findDistinctSanPhamMau();
}
