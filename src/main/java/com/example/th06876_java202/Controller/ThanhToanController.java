package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DiaChi;
import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Service.DiaChiService;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Service.HoaDonService;
import com.example.th06876_java202.Service.KhachHangService;
import com.example.th06876_java202.Storefront.DatHangException;
import com.example.th06876_java202.Storefront.DonHangOnlineService;
import com.example.th06876_java202.Storefront.GioHang;
import com.example.th06876_java202.Storefront.GioHangService;
import com.example.th06876_java202.Storefront.GioHangView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Thanh toán / đặt hàng online. Toàn bộ các đường dẫn /thanh-toan/** yêu cầu đăng nhập
 * bằng tài khoản khách hàng (đã cấu hình trong SecurityConfig).
 */
@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController {

    private final GioHang gioHang;
    private final GioHangService gioHangService;
    private final KhachHangService khachHangService;
    private final DiaChiService diaChiService;
    private final DonHangOnlineService donHangOnlineService;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;

    public static final String SO_TAI_KHOAN_NGAN_HANG = "0123456789";
    public static final String TEN_NGAN_HANG = "Ngân hàng TMCP Ngoại Thương Việt Nam (Vietcombank)";
    public static final String CHU_TAI_KHOAN = "CONG TY TNHH FS SHOES";

    private KhachHang khachHangHienTai(Authentication authentication) {
        return khachHangService.findByTenDangNhap(authentication.getName());
    }

    @GetMapping
    public String trangThanhToan(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        KhachHang kh = khachHangHienTai(authentication);
        if (kh == null) {
            redirectAttributes.addFlashAttribute("loiTaiKhoan", "Không tìm thấy hồ sơ khách hàng cho tài khoản này.");
            return "redirect:/gio-hang";
        }

        GioHangView view = gioHangService.xemGioHang(gioHang, kh.getMaKH());
        if (view.getDongHang().isEmpty() || view.getTongTienHang().signum() <= 0) {
            redirectAttributes.addFlashAttribute("thongBaoGioHang", "Giỏ hàng của bạn đang trống, vui lòng chọn sản phẩm trước khi thanh toán.");
            return "redirect:/gio-hang";
        }

        List<DiaChi> diaChis = diaChiService.findByKhachHang(kh.getMaKH());

        model.addAttribute("gioHang", view);
        model.addAttribute("diaChis", diaChis);
        model.addAttribute("khachHang", kh);
        return "thanhtoan/index";
    }

    @PostMapping("/dat-hang")
    public String datHang(@RequestParam String chonDiaChi,
                           @RequestParam(required = false) String tenNguoiNhan,
                           @RequestParam(required = false) String soDienThoaiNguoiNhan,
                           @RequestParam(required = false) String tinhThanh,
                           @RequestParam(required = false) String quanHuyen,
                           @RequestParam(required = false) String phuongXa,
                           @RequestParam(required = false) String diaChiCuThe,
                           @RequestParam(defaultValue = "false") boolean luuDiaChi,
                           @RequestParam(defaultValue = "false") boolean datMacDinh,
                           @RequestParam String phuongThucThanhToan,
                           @RequestParam(required = false) String ghiChu,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        KhachHang kh = khachHangHienTai(authentication);
        if (kh == null) {
            redirectAttributes.addFlashAttribute("loiDatHang", "Không tìm thấy hồ sơ khách hàng cho tài khoản này.");
            return "redirect:/thanh-toan";
        }

        String diaChiGiaoHangText;
        try {
            if ("moi".equals(chonDiaChi)) {
                if (isBlank(tenNguoiNhan) || isBlank(soDienThoaiNguoiNhan) || isBlank(tinhThanh)
                        || isBlank(quanHuyen) || isBlank(phuongXa) || isBlank(diaChiCuThe)) {
                    redirectAttributes.addFlashAttribute("loiDatHang", "Vui lòng nhập đầy đủ thông tin địa chỉ giao hàng mới.");
                    return "redirect:/thanh-toan";
                }
                if (!soDienThoaiNguoiNhan.matches("^(0(3|5|7|8|9))[0-9]{8}$")) {
                    redirectAttributes.addFlashAttribute("loiDatHang", "Số điện thoại người nhận không hợp lệ.");
                    return "redirect:/thanh-toan";
                }

                DiaChi diaChiMoi = new DiaChi();
                diaChiMoi.setKhachHang(kh);
                diaChiMoi.setTenNguoiNhan(tenNguoiNhan.trim());
                diaChiMoi.setSoDienThoaiNguoiNhan(soDienThoaiNguoiNhan.trim());
                diaChiMoi.setTinhThanh(tinhThanh.trim());
                diaChiMoi.setQuanHuyen(quanHuyen.trim());
                diaChiMoi.setPhuongXa(phuongXa.trim());
                diaChiMoi.setDiaChiCuThe(diaChiCuThe.trim());

                boolean chuaCoDiaChiNao = diaChiService.findByKhachHang(kh.getMaKH()).isEmpty();
                diaChiMoi.setDiaChiMacDinh(datMacDinh || chuaCoDiaChiNao);

                diaChiGiaoHangText = dinhDangDiaChi(diaChiMoi);

                if (luuDiaChi || chuaCoDiaChiNao) {
                    diaChiService.save(diaChiMoi);
                }
            } else {
                Integer maDiaChi;
                try {
                    maDiaChi = Integer.parseInt(chonDiaChi);
                } catch (NumberFormatException ex) {
                    redirectAttributes.addFlashAttribute("loiDatHang", "Vui lòng chọn địa chỉ giao hàng.");
                    return "redirect:/thanh-toan";
                }
                DiaChi diaChi = diaChiService.findById(maDiaChi).orElse(null);
                if (diaChi == null || diaChi.getKhachHang() == null || !diaChi.getKhachHang().getMaKH().equals(kh.getMaKH())) {
                    redirectAttributes.addFlashAttribute("loiDatHang", "Địa chỉ giao hàng không hợp lệ.");
                    return "redirect:/thanh-toan";
                }
                diaChiGiaoHangText = dinhDangDiaChi(diaChi);
            }

            HoaDon hoaDon = donHangOnlineService.datHang(kh, gioHang, diaChiGiaoHangText, phuongThucThanhToan, ghiChu);
            return "redirect:/thanh-toan/thanh-cong/" + hoaDon.getMaHoaDon();

        } catch (DatHangException ex) {
            redirectAttributes.addFlashAttribute("loiDatHang", ex.getMessage());
            return "redirect:/thanh-toan";
        }
    }

    @GetMapping("/thanh-cong/{id}")
    public String thanhCong(@PathVariable Integer id, Model model, Authentication authentication) {
        KhachHang kh = khachHangHienTai(authentication);
        HoaDon hoaDon = hoaDonService.findById(id).orElse(null);
        if (hoaDon == null || kh == null || hoaDon.getMaKhachHang() == null
                || !hoaDon.getMaKhachHang().getMaKH().equals(kh.getMaKH())) {
            return "redirect:/";
        }

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("chiTiet", hoaDonChiTietService.findByHoaDOn(hoaDon));
        model.addAttribute("soTaiKhoan", SO_TAI_KHOAN_NGAN_HANG);
        model.addAttribute("tenNganHang", TEN_NGAN_HANG);
        model.addAttribute("chuTaiKhoan", CHU_TAI_KHOAN);
        return "thanhtoan/thanh-cong";
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String dinhDangDiaChi(DiaChi d) {
        return d.getTenNguoiNhan() + " - " + d.getSoDienThoaiNguoiNhan() + " | "
                + d.getDiaChiCuThe() + ", " + d.getPhuongXa() + ", " + d.getQuanHuyen() + ", " + d.getTinhThanh();
    }
}
