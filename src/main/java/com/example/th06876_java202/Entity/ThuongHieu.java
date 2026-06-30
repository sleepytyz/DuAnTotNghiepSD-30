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
@Table(name = "ThuongHieu")
public class ThuongHieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThuongHieu")
    private Integer maThuongHieu;

    @NotBlank(message = "Không bỏ trống thương hiệu")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Thương hiệu chỉ chứa chữ cái, khoảng trắng ")
    @Column(name = "TenThuongHieu")
    private String tenThuongHieu;

    @Column(name = "TrangThai")
    private boolean trangThai;

}
