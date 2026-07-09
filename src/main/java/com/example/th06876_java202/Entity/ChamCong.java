package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ChamCong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChamCong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChamCong")
    private Integer maChamCong;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "MaCa")
    private CaLamViec caLamViec;

    @Column(name = "NgayChamCong")
    private LocalDate ngayChamCong;

    @Column(name = "GioVao")
    private LocalTime gioVao;

    @Column(name = "GioRa")
    private LocalTime gioRa;

    @Column(name = "TrangThai")
    private Boolean trangThai; // false: đã qua (đã chấm công), true: sắp tới (lịch đã xếp)

    @Column(name = "SoGioLam")
    private BigDecimal soGioLam;

    @Column(name = "GhiChu")
    private String ghiChu;

    // [SỬA] Cờ tạm (KHÔNG map vào DB) đánh dấu bản ghi này có ngày = hôm nay hay không.
    // Tính sẵn ở controller để template chỉ cần đọc chamCong.laHomNay, tránh dùng
    // Set.contains() trong Thymeleaf (dễ sai do lệch kiểu Integer/Long).
    @Transient
    private boolean laHomNay;
}