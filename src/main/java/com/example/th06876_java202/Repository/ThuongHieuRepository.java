package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ThuongHieu;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, String> {

    boolean existsByTenThuongHieu(String thuongHieu);

    Page<ThuongHieu> findAllByOrderByNgayTaoDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update ThuongHieu set TrangThai = 0 where MaThuongHieu = ?", nativeQuery = true)
    int capnhattt(String maThuongHieu);

    @Query("SELECT COUNT(t) > 0 FROM ThuongHieu t WHERE LOWER(REPLACE(t.tenThuongHieu, ' ', '')) = LOWER(REPLACE(:ten, ' ', ''))")
    boolean existsByTenThuongHieuNormalized(@Param("ten") String ten);
}