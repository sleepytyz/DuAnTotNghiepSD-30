package com.example.th06876_java202.Storefront;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Dữ liệu form đăng ký tài khoản khách hàng (gộp cả thông tin đăng nhập và hồ sơ khách hàng
 * để hiển thị/validate trên 1 form duy nhất ở trang đăng ký).
 */
@Data
public class DangKyKhachHangDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Tên đăng nhập chỉ gồm chữ, số, gạch dưới, không dấu cách")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, max = 100, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String xnMatKhau;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0(3|5|7|8|9))[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam (10 số)")
    private String sdt;

    @NotBlank(message = "Email không được để trống")
    @jakarta.validation.constraints.Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
}
