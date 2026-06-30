package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.Storefront.DatHangException;
import com.example.th06876_java202.Storefront.DoiMatKhauDTO;
import com.example.th06876_java202.Storefront.DonHangOnlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Khu vực "Tài khoản của tôi" cho khách hàng đã đăng nhập (yêu cầu ROLE_USER, đã cấu hình ở SecurityConfig).
 */
@Controller
@RequestMapping("/ca-nhan")
@RequiredArgsConstructor
public class TaiKhoanCaNhanController {

    private final KhachHangService khachHangService;
    private final TaiKhoanService taiKhoanService;
    private final DiaChiService diaChiService;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final DonHangOnlineService donHangOnlineService;
    private final GiamGiaService giamGiaService;

    private KhachHang khachHangHienTai(Authentication authentication) {
        return khachHangService.findByTenDangNhap(authentication.getName());
    }

    // ====== Hồ sơ cá nhân ======

    @GetMapping
    public String hoSo(Model model, Authentication authentication) {
        model.addAttribute("khachHang", khachHangHienTai(authentication));
        return "taikhoan/ho-so";
    }

    @PostMapping("/cap-nhat")
    public String capNhatHoSo(@Valid @ModelAttribute("khachHang") KhachHang form,
                               BindingResult result,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        KhachHang hienTai = khachHangHienTai(authentication);
        if (hienTai == null) return "redirect:/";

        // không cho đổi các trường nhạy cảm qua form này
        form.setMaKH(hienTai.getMaKH());
        form.setTaiKhoan(hienTai.getTaiKhoan());
        form.setNgayDangKy(hienTai.getNgayDangKy());
        form.setTrangThai(hienTai.isTrangThai());
        form.setDanhSachDiaChi(hienTai.getDanhSachDiaChi());

        khachHangService.validateKhachHang(form, result);

        if (result.hasErrors()) {
            model.addAttribute("khachHang", form);
            return "taikhoan/ho-so";
        }

        khachHangService.save(form);
        redirectAttributes.addFlashAttribute("thongBao", "Cập nhật thông tin cá nhân thành công.");
        return "redirect:/ca-nhan";
    }

    // ====== Đổi mật khẩu ======

    @GetMapping("/doi-mat-khau")
    public String trangDoiMatKhau(Model model) {
        model.addAttribute("doiMatKhau", new DoiMatKhauDTO());
        return "taikhoan/doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String xuLyDoiMatKhau(@Valid @ModelAttribute("doiMatKhau") DoiMatKhauDTO dto,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (!result.hasErrors() && !dto.getMatKhauMoi().equals(dto.getXnMatKhauMoi())) {
            result.rejectValue("xnMatKhauMoi", "error.doiMatKhau", "Mật khẩu xác nhận không khớp.");
        }
        if (result.hasErrors()) {
            return "taikhoan/doi-mat-khau";
        }

        KhachHang kh = khachHangHienTai(authentication);
        String loi = taiKhoanService.doiMatKhau(kh.getTaiKhoan().getMaTaiKhoan(), dto.getMatKhauCu(), dto.getMatKhauMoi());
        if (loi != null) {
            model.addAttribute("loiMatKhauCu", loi);
            return "taikhoan/doi-mat-khau";
        }

        redirectAttributes.addFlashAttribute("thongBao", "Đổi mật khẩu thành công.");
        return "redirect:/ca-nhan";
    }

    // ====== Sổ địa chỉ ======

    @GetMapping("/dia-chi")
    public String soDiaChi(Model model, Authentication authentication) {
        KhachHang kh = khachHangHienTai(authentication);
        model.addAttribute("diaChis", diaChiService.findByKhachHang(kh.getMaKH()));
        model.addAttribute("diaChiMoi", new DiaChi());
        return "taikhoan/dia-chi";
    }

    @PostMapping("/dia-chi/them")
    public String themDiaChi(@Valid @ModelAttribute("diaChiMoi") DiaChi diaChi,
                              BindingResult result,
                              @RequestParam(defaultValue = "false") boolean macDinh,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        KhachHang kh = khachHangHienTai(authentication);
        if (result.hasErrors()) {
            model.addAttribute("diaChis", diaChiService.findByKhachHang(kh.getMaKH()));
            return "taikhoan/dia-chi";
        }
        diaChi.setMaDiaChi(null);
        diaChi.setKhachHang(kh);
        boolean chuaCoDiaChiNao = diaChiService.findByKhachHang(kh.getMaKH()).isEmpty();
        diaChi.setDiaChiMacDinh(macDinh || chuaCoDiaChiNao);
        diaChiService.save(diaChi);
        redirectAttributes.addFlashAttribute("thongBao", "Đã thêm địa chỉ mới.");
        return "redirect:/ca-nhan/dia-chi";
    }

    @PostMapping("/dia-chi/sua")
    public String suaDiaChi(@Valid @ModelAttribute("diaChiMoi") DiaChi diaChi,
                             BindingResult result,
                             @RequestParam(defaultValue = "false") boolean macDinh,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        KhachHang kh = khachHangHienTai(authentication);
        DiaChi cu = diaChiService.findById(diaChi.getMaDiaChi()).orElse(null);
        if (cu == null || cu.getKhachHang() == null || !cu.getKhachHang().getMaKH().equals(kh.getMaKH())) {
            redirectAttributes.addFlashAttribute("loiDiaChi", "Địa chỉ không hợp lệ.");
            return "redirect:/ca-nhan/dia-chi";
        }
        if (result.hasErrors()) {
            model.addAttribute("diaChis", diaChiService.findByKhachHang(kh.getMaKH()));
            return "taikhoan/dia-chi";
        }
        diaChi.setKhachHang(kh);
        diaChi.setDiaChiMacDinh(macDinh);
        diaChiService.save(diaChi);
        redirectAttributes.addFlashAttribute("thongBao", "Đã cập nhật địa chỉ.");
        return "redirect:/ca-nhan/dia-chi";
    }

    @PostMapping("/dia-chi/xoa/{id}")
    public String xoaDiaChi(@PathVariable Integer id, Authentication authentication, RedirectAttributes redirectAttributes) {
        KhachHang kh = khachHangHienTai(authentication);
        DiaChi dc = diaChiService.findById(id).orElse(null);
        if (dc != null && dc.getKhachHang() != null && dc.getKhachHang().getMaKH().equals(kh.getMaKH())) {
            diaChiService.delete(id);
            redirectAttributes.addFlashAttribute("thongBao", "Đã xoá địa chỉ.");
        }
        return "redirect:/ca-nhan/dia-chi";
    }

    @PostMapping("/dia-chi/mac-dinh/{id}")
    public String datMacDinh(@PathVariable Integer id, Authentication authentication, RedirectAttributes redirectAttributes) {
        KhachHang kh = khachHangHienTai(authentication);
        DiaChi dc = diaChiService.findById(id).orElse(null);
        if (dc != null && dc.getKhachHang() != null && dc.getKhachHang().getMaKH().equals(kh.getMaKH())) {
            dc.setDiaChiMacDinh(true);
            diaChiService.save(dc);
            redirectAttributes.addFlashAttribute("thongBao", "Đã đặt làm địa chỉ mặc định.");
        }
        return "redirect:/ca-nhan/dia-chi";
    }

    // ====== Đơn hàng của tôi ======

    @GetMapping("/don-hang")
    public String donHang(@RequestParam(defaultValue = "0") int page, Model model, Authentication authentication) {
        KhachHang kh = khachHangHienTai(authentication);
        Page<HoaDon> trang = hoaDonService.findByKhachHang(kh.getMaKH(), PageRequest.of(Math.max(page, 0), 8));
        model.addAttribute("trang", trang);
        return "taikhoan/don-hang";
    }

    @GetMapping("/don-hang/{id}")
    public String chiTietDonHang(@PathVariable Integer id, Model model, Authentication authentication) {
        KhachHang kh = khachHangHienTai(authentication);
        HoaDon hoaDon = hoaDonService.findById(id).orElse(null);
        if (hoaDon == null || hoaDon.getMaKhachHang() == null || !hoaDon.getMaKhachHang().getMaKH().equals(kh.getMaKH())) {
            return "redirect:/ca-nhan/don-hang";
        }
        List<HoaDonChiTiet> chiTiet = hoaDonChiTietService.findByHoaDOn(hoaDon);
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("chiTiet", chiTiet);
        model.addAttribute("coTheHuy", "Chờ xác nhận".equals(hoaDon.getTrangThai()));
        return "taikhoan/don-hang-chi-tiet";
    }

    @PostMapping("/don-hang/huy/{id}")
    public String huyDonHang(@PathVariable Integer id, Authentication authentication, RedirectAttributes redirectAttributes) {
        KhachHang kh = khachHangHienTai(authentication);
        HoaDon hoaDon = hoaDonService.findById(id).orElse(null);
        if (hoaDon == null || hoaDon.getMaKhachHang() == null || !hoaDon.getMaKhachHang().getMaKH().equals(kh.getMaKH())) {
            redirectAttributes.addFlashAttribute("loiDonHang", "Đơn hàng không hợp lệ.");
            return "redirect:/ca-nhan/don-hang";
        }
        try {
            donHangOnlineService.khachHuyDon(hoaDon);
            redirectAttributes.addFlashAttribute("thongBao", "Đã huỷ đơn hàng #" + id + ".");
        } catch (DatHangException ex) {
            redirectAttributes.addFlashAttribute("loiDonHang", ex.getMessage());
        }
        return "redirect:/ca-nhan/don-hang/" + id;
    }

    // ====== Voucher của tôi ======

    @GetMapping("/voucher")
    public String voucherCuaToi(Model model, Authentication authentication) {
        KhachHang kh = khachHangHienTai(authentication);
        model.addAttribute("vouchers", giamGiaService.getVoucherKhaDungChoKhachHang(kh.getMaKH()));
        return "taikhoan/voucher";
    }
}
