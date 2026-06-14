package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.MauSac;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    boolean existsByTenMauSac(String tenMauSac);
}
