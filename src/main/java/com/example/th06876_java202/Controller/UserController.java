package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final TaiKhoanService taiKhoanService;
    private final HoaDonService hoaDonService;
    private final KhachHangService khachHangService;
    private final SanPhamService sanPhamService;
    private final SanPhamChiTietService sanPhamChiTietService;
    private final ThongKeService thongKeService;

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        if(authentication != null && authentication.isAuthenticated()){
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

            if (isAdmin) {
                // === DỮ LIỆU CHO ADMIN ===
                // Thống kê tổng quan
                ThongKeTongQuanDTO tongQuan = thongKeService.thongKeTongQuan();
                model.addAttribute("tongQuan", tongQuan);

                // Thống kê theo ngày cho biểu đồ (7 ngày gần nhất)
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7);
                List<ThongKeDoanhThuDTO> thongKeNgay = thongKeService.thongKeDoanhThuTheoNgay(startDate, endDate);
                model.addAttribute("thongKeNgay", thongKeNgay);

                // Tổng số khách hàng
                List<KhachHang> khachHangs = khachHangService.getAllKhachHang();
                model.addAttribute("tongKhachHang", khachHangs != null ? khachHangs.size() : 0);

                // Tổng số sản phẩm
                List<SanPham> sanPhams = sanPhamService.getAll();
                model.addAttribute("tongSanPham", sanPhams != null ? sanPhams.size() : 0);

                // Số sản phẩm sắp hết (tồn kho <= 10)
                List<SanPhamChiTiet> spct = sanPhamChiTietService.getall();
                long sapHet = spct.stream()
                        .filter(s -> s.getSoLuongTon() != null && s.getSoLuongTon() <= 10 && s.getSoLuongTon() > 0)
                        .count();
                model.addAttribute("sanPhamSapHet", sapHet);

                // Đơn hàng gần đây (10 đơn)
                List<HoaDon> hoaDonGanDay = hoaDonService.getAll().stream()
                        .limit(10)
                        .toList();
                model.addAttribute("hoaDonGanDay", hoaDonGanDay);

                // Thống kê trạng thái đơn hàng
                long dangXuLy = hoaDonService.getAll().stream()
                        .filter(h -> "Đang xử lý".equals(h.getTrangThai()) || "Chờ xác nhận".equals(h.getTrangThai()))
                        .count();
                long daGiao = hoaDonService.getAll().stream()
                        .filter(h -> "Đã giao".equals(h.getTrangThai()))
                        .count();
                long daHuy = hoaDonService.getAll().stream()
                        .filter(h -> "Đã hủy".equals(h.getTrangThai()))
                        .count();
                model.addAttribute("donDangXuLy", dangXuLy);
                model.addAttribute("donDaGiao", daGiao);
                model.addAttribute("donDaHuy", daHuy);

                // Đơn hàng mới
                long donMoi = hoaDonService.getAll().stream()
                        .filter(h -> "Chờ xác nhận".equals(h.getTrangThai()))
                        .count();
                model.addAttribute("tongDonHangMoi", donMoi);

                return "account/admin/home";
            } else if (isStaff) {
                // === DỮ LIỆU CHO STAFF ===
                // Thống kê theo ngày cho biểu đồ (7 ngày gần nhất)
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7);
                List<ThongKeDoanhThuDTO> thongKeNgay = thongKeService.thongKeDoanhThuTheoNgay(startDate, endDate);
                model.addAttribute("thongKeNgay", thongKeNgay);

                // Đơn hàng gần đây (10 đơn)
                List<HoaDon> hoaDonGanDay = hoaDonService.getAll().stream()
                        .limit(10)
                        .toList();
                model.addAttribute("hoaDonGanDay", hoaDonGanDay);

                // Thống kê hôm nay
                LocalDate today = LocalDate.now();

                // Đơn hàng hôm nay - ngayTao là LocalDate, so sánh trực tiếp
                List<HoaDon> hoaDonHomNay = hoaDonService.getAll().stream()
                        .filter(h -> h.getNgayTao() != null && h.getNgayTao().equals(today))
                        .toList();
                model.addAttribute("donHomNay", hoaDonHomNay.size());

                // Doanh thu hôm nay
                double doanhThuHomNay = hoaDonHomNay.stream()
                        .filter(h -> "Đã giao".equals(h.getTrangThai()))
                        .mapToDouble(h -> h.getTongTien() != null ? h.getTongTien().doubleValue() : 0)
                        .sum();
                model.addAttribute("doanhThuHomNay", String.format("%,.0f", doanhThuHomNay));

                // Đơn đang xử lý
                long dangXuLy = hoaDonService.getAll().stream()
                        .filter(h -> "Đang xử lý".equals(h.getTrangThai()) || "Chờ xác nhận".equals(h.getTrangThai()))
                        .count();
                model.addAttribute("donDangXuLy", dangXuLy);

                // Khách mới hôm nay - SỬA: dùng ngayDangKy
                List<KhachHang> khachHangs = khachHangService.getAllKhachHang();
                long khachMoi = khachHangs.stream()
                        .filter(k -> k.getNgayDangKy() != null && k.getNgayDangKy().equals(today))
                        .count();
                model.addAttribute("khachMoiHomNay", khachMoi);

                // Đơn mới (chưa xử lý)
                long donMoi = hoaDonService.getAll().stream()
                        .filter(h -> "Chờ xác nhận".equals(h.getTrangThai()))
                        .count();
                model.addAttribute("donHangMoi", donMoi);

                return "account/staff/home";
            }
        }
        return "sanpham/index";
    }

    @GetMapping("/login")
    public String login() {
        return "account/user/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        return "account/user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute TaiKhoan taiKhoan,
                           @RequestParam String xnmatKhau,
                           Model model) {
        if (!taiKhoan.getMatKhau().equals(xnmatKhau)){
            model.addAttribute("error", "Mật khẩu không khớp");
            return "account/user/register";
        }
        if (taiKhoanService.isTenDangNhapExist(taiKhoan.getTenDangNhap())) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "account/user/register";
        }
        taiKhoanService.createUser(taiKhoan);
        return "redirect:/login?registered";
    }

    @GetMapping("/accessDenied")
    public String deny() {
        return "account/user/deny";
    }
}