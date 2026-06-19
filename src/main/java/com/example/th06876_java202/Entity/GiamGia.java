package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "GiamGia")
public class GiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiamGia")
    private int maGiamGia;

    @NotBlank(message = "Tên chương trình giảm giá không được để trống")
    @Size(max = 255, message = "Tên chương trình không được vượt quá 255 ký tự")
    @Column(name = "TenChuongTrinh")
    private String tenGiamGia;

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Column(name = "LoaiGiamGia")
    private String loaiGiamGia;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    @Column(name = "GiaTriGiam")
    private BigDecimal giaTriGiam;

    @DecimalMin(value = "0.0", message = "Giá trị giảm tối đa phải lớn hơn hoặc bằng 0")
    @Column(name = "GiamToiDa")
    private BigDecimal giamToiDa;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là hiện tại hoặc trong tương lai")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "NgayBatDau")
    private LocalDateTime ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là một ngày trong tương lai")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "NgayKetThuc")
    private LocalDateTime ngayKetThuc;

    @NotBlank(message = "Trạng thái không được để trống")
    @Column(name = "TrangThai")
    private String trangThai;

    @NotBlank(message = "Loại voucher không được để trống")
    @Column(name = "LoaiVoucher")
    private String loaiVoucher;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng mã giảm giá phải lớn hơn hoặc bằng 0")
    @Column(name = "SoLuong")
    private Integer soLuong;

    @AssertTrue(message = "Nếu loại giảm giá là phần trăm, giá trị giảm phải từ 1 đến 100")
    private boolean isGiaTriGiamValid() {
        if (loaiGiamGia == null || giaTriGiam == null) {
            return true;
        }
        if (loaiGiamGia.equalsIgnoreCase("PhanTram") || loaiGiamGia.equalsIgnoreCase("Percentage")) {
            double value = giaTriGiam.doubleValue();
            return value >= 1.0 && value <= 100.0;
        }

        return true;
    }

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    private boolean isNgayKetThucValid() {
        if (ngayBatDau == null || ngayKetThuc == null) {
            return true;
        }
        return ngayKetThuc.isAfter(ngayBatDau);
    }
}