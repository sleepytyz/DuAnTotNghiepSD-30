package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ThuongHieu;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {

    boolean existsByTenThuongHieu(String thuongHieu);

    Page<ThuongHieu> findAllByOrderByMaThuongHieuDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update ThuongHieu set TrangThai = 0 where MaThuongHieu = ?", nativeQuery = true)
    int capnhattt(Integer maThuongHieu);
}
