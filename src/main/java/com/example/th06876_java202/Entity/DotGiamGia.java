package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DotGiamGia", schema = "dbo")
public class DotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiamGia")
    private Integer maGiamGia;

    @Column(name = "TenGiamGia")
    private String tenGiamGia;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "LoaiGiamGia")
    private String loaiGiamGia;

    @Column(name = "GiaTriGiam")
    private BigDecimal giaTriGiam;

    @Column(name = "GiamToiDa")
    private BigDecimal giamToiDa;

    @Column(name = "NgayBatDau")
    private Date ngayBatDau;

    @Column(name = "NgayKetThuc")
    private Date ngayKetThuc;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "MaNhanVienTao")
    private Integer maNhanVienTao;

    @Column(name = "NgayTao")
    private Date ngayTao;
}
