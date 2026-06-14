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
@Table(name = "KichThuoc")
public class KichThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKichThuoc")
    private Integer maKichThuoc;

    @NotBlank(message = "Không bỏ trống kích thước")
    @Pattern(regexp = "^\\d+$", message = "Kích thước chỉ chứa số")
    @Column(name = "TenKichThuoc")
    private String tenKichThuoc;
}
