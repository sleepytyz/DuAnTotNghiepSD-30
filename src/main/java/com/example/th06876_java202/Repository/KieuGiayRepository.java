package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.KieuGiay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface KieuGiayRepository extends JpaRepository<KieuGiay, String> {

    boolean existsByTenKieuGiay(String tenKieuGiay);

    Page<KieuGiay> findAllByOrderByNgayTaoDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update KieuGiay set TrangThai = 0 where MaKieuGiay = ?", nativeQuery = true)
    int capnhatt(String maKieuGiay);

    @Query("SELECT COUNT(k) > 0 FROM KieuGiay k WHERE LOWER(REPLACE(k.tenKieuGiay, ' ', '')) = LOWER(REPLACE(:ten, ' ', ''))")
    boolean existsByTenKieuGiayNormalized(@Param("ten") String ten);
}