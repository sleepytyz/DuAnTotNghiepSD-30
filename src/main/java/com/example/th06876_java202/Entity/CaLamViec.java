package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "CaLamViec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaLamViec {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCa")
    private Integer maCa;

    @NotBlank(message = "Tên ca không được để trống")
    @Column(name = "TenCa")
    private String tenCa;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @Column(name = "GioBatDau")
    private LocalTime gioBatDau;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @Column(name = "GioKetThuc")
    private LocalTime gioKetThuc;

    @Column(name = "MoTa")
    private String moTa;

    @OneToMany(mappedBy = "caLamViec", fetch = FetchType.LAZY)
    private List<ChamCong> danhSachChamCong;
}