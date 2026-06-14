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
@Table(name = "MauSac")
public class MauSac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaMauSac")
    private Integer maMauSac;

    @NotBlank(message = "Không bỏ trống màu sắc")
    @Pattern(
            regexp = "^[\\p{L}\\d\\s\\+]*$",
            message = "Màu sắc chỉ chứa chữ cái, số, khoảng trắng và dấu +"
    )
    @Column(name = "TenMauSac")
    private String tenMauSac;
}
