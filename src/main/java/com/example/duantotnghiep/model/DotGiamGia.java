package com.example.duantotnghiep.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DotGiamGia")
public class DotGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiamGia")
    private int maGiamGia;

    @NotBlank(message = "Tên đợt giảm giá không được để trống")
    @Column(name = "TenGiamGia")
    private String tenGiamGia;

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Column(name = "LoaiGiamGia")
    private String loaiGiamGia;

    @NotBlank(message = "Giá trị giảm không được để trống")
    @Column(name = "GiaTriGiam")
    private String giaTriGiam;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "TrangThai")
    private int trangThai;
}
