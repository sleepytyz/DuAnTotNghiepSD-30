package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {
    List<DotGiamGia> findByTenGiamGiaContainingIgnoreCase(String keyword);
}
