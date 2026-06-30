package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "KieuGiay")
public class KieuGiay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKieuGiay")
    private Integer maKieuGiay;

    @NotBlank(message = "Không bỏ trống kiểu giày")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Kiểu giày chỉ chứa chữ cái, khoảng trắng ")
    @Column(name = "TenKieuGiay")
    private String tenKieuGiay;

    @Column(name = "TrangThai")
    private boolean trangThai;

}
