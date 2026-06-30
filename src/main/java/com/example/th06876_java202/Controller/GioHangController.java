package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Service.KhachHangService;
import com.example.th06876_java202.Storefront.GioHang;
import com.example.th06876_java202.Storefront.GioHangService;
import com.example.th06876_java202.Storefront.GioHangView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Giỏ hàng của khách (lưu theo session, không cần đăng nhập mới xem/thêm được).
 */
@Controller
@RequestMapping("/gio-hang")
@RequiredArgsConstructor
public class GioHangController {

    private final GioHang gioHang;
    private final GioHangService gioHangService;
    private final KhachHangService khachHangService;

    private Integer maKhachHangHienTai(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        KhachHang kh = khachHangService.findByTenDangNhap(authentication.getName());
        return kh != null ? kh.getMaKH() : null;
    }

    @GetMapping
    public String xemGioHang(Model model, Authentication authentication) {
        GioHangView view = gioHangService.xemGioHang(gioHang, maKhachHangHienTai(authentication));
        model.addAttribute("gioHang", view);
        return "giohang/index";
    }

    /** Thêm vào giỏ qua form HTML thông thường (dùng khi JS bị tắt). */
    @PostMapping("/them")
    public String themVaoGio(@RequestParam Integer maSanPhamChiTiet,
                              @RequestParam(defaultValue = "1") Integer soLuong,
                              @RequestParam(required = false) String redirect,
                              RedirectAttributes redirectAttributes) {
        String loi = gioHangService.themVaoGio(gioHang, maSanPhamChiTiet, soLuong);
        redirectAttributes.addFlashAttribute("thongBaoGioHang", loi != null ? loi : "Đã thêm sản phẩm vào giỏ hàng.");
        return "redirect:" + (redirect != null && !redirect.isBlank() ? redirect : "/gio-hang");
    }

    /** Thêm vào giỏ qua AJAX (dùng cho nút "Thêm vào giỏ" trên trang danh sách/chi tiết, không tải lại trang). */
    @PostMapping("/api/them")
    @ResponseBody
    public Map<String, Object> themVaoGioAjax(@RequestParam Integer maSanPhamChiTiet,
                                                @RequestParam(defaultValue = "1") Integer soLuong) {
        Map<String, Object> ket = new HashMap<>();
        String loi = gioHangService.themVaoGio(gioHang, maSanPhamChiTiet, soLuong);
        ket.put("thanhCong", loi == null);
        ket.put("thongBao", loi == null ? "Đã thêm vào giỏ hàng." : loi);
        ket.put("tongSoLuong", gioHang.tongSoLuong());
        return ket;
    }

    @PostMapping("/cap-nhat")
    public String capNhatSoLuong(@RequestParam Integer maSanPhamChiTiet, @RequestParam Integer soLuong) {
        gioHangService.capNhatSoLuong(gioHang, maSanPhamChiTiet, soLuong);
        return "redirect:/gio-hang";
    }

    @PostMapping("/xoa")
    public String xoaSanPham(@RequestParam Integer maSanPhamChiTiet) {
        gioHang.xoaSanPham(maSanPhamChiTiet);
        return "redirect:/gio-hang";
    }

    @PostMapping("/ap-dung-voucher")
    public String apDungVoucher(@RequestParam String maVoucher, RedirectAttributes redirectAttributes, Authentication authentication) {
        String loi = gioHangService.apDungVoucher(gioHang, maKhachHangHienTai(authentication), maVoucher);
        if (loi != null) {
            redirectAttributes.addFlashAttribute("loiVoucher", loi);
        }
        return "redirect:/gio-hang";
    }

    @PostMapping("/bo-voucher")
    public String boVoucher() {
        gioHangService.boVoucher(gioHang);
        return "redirect:/gio-hang";
    }
}
