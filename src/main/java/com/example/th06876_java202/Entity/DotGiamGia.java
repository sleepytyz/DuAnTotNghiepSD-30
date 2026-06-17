package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @NotBlank(message = "Tên đợt giảm giá không được để trống!")
    @Column(name = "TenGiamGia")
    private String tenGiamGia;

    @Column(name = "MoTa")
    private String moTa;

    @NotBlank(message = "Vui lòng chọn loại giảm giá!")
    @Column(name = "LoaiGiamGia")
    private String loaiGiamGia;

    @NotNull(message = "Giá trị giảm không được để trống!")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0!")
    @Column(name = "GiaTriGiam")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Mức giảm tối đa không được để trống!")
    @DecimalMin(value = "0.0", inclusive = true, message = "Mức giảm tối đa không được âm!")
    @Column(name = "GiamToiDa")
    private BigDecimal giamToiDa;

    @NotNull(message = "Ngày bắt đầu không được để trống!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @NotNull(message = "Vui lòng chọn trạng thái hoạt động!")
    @Column(name = "TrangThai")
    private Boolean trangThai;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayTao")
    private LocalDate ngayTao;


}