package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SanPhamHinhAnh")
public class SanPhamHinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnh")
    private Integer MaHinhAnh;

    @ManyToOne
    @JoinColumn(name = "MaSanPhamChiTiet")
    private SanPhamChiTiet sanPhamChiTiet;

    @Lob
    @Column(name = "HinhAnh", nullable = false)
    private byte[] HinhAnh;

    @Column(name = "LaAnhChinh")
    private Boolean laAnhChinh;
}
