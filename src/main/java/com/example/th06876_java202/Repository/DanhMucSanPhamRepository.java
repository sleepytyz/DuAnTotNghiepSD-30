package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DanhMucSanPhamRepository extends JpaRepository<DanhMucSanPham, String> {

    @Modifying
    @Transactional
    @Query(value = "update DanhMucSanPham set TrangThai = 0 where MaDanhMuc = ?", nativeQuery = true)
    int updateTrangThai(String maDanhMuc);

    List<DanhMucSanPham> findByMaDanhMuc(String idd);

    boolean existsByTenDanhMuc(@NotBlank(message = "Tên danh mục không để trống") String tenDanhMuc);

    Page<DanhMucSanPham> findAllByOrderByNgayTaoDesc(Pageable pageable);

    boolean existsByTenDanhMucIgnoreCase(String tenDanhMuc);
}