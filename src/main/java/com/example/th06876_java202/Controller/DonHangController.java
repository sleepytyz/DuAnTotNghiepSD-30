package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Entity.HoaDonChiTiet;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/donhang")
public class DonHangController {

    @Autowired
    private HoaDonService service;

    private final HoaDonChiTietService hoaDonChiTietService;

    public DonHangController(HoaDonChiTietService hoaDonChiTietService) {
        this.hoaDonChiTietService = hoaDonChiTietService;
    }

    @GetMapping("/donhang")
    public String hoaDon(Model model) {
        model.addAttribute("pageTitle", "Hóa đơn");
        return "donhang/index";
    }

    @GetMapping("/index")
    public String index(
            @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String mahd,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) LocalDateTime ngay,
            @RequestParam(required = false) LocalDateTime ngay2,
            Model model) {

        model.addAttribute("activeMenu", "donhang");

        // Danh sách trạng thái được phép hiển thị
        List<String> allowedStatuses = Arrays.asList("Chờ xác nhận", "Đã xác nhận", "Đang giao");

        Page<HoaDon> page = null;

        // Xử lý lọc theo trạng thái
        if (tt != null && !tt.trim().isEmpty()) {
            // Nếu trạng thái được chọn nằm trong danh sách cho phép
            if (allowedStatuses.contains(tt)) {
                page = service.findByTrangThai(tt, pageable);
            } else {
                // Nếu chọn trạng thái không được phép, vẫn hiển thị các trạng thái cho phép
                page = service.findByTrangThaiIn(allowedStatuses, pageable);
            }
        } else if (ngay != null || ngay2 != null) {
            // Lọc theo ngày và chỉ lấy các trạng thái cho phép
            page = service.searchByNgayTaodhAndStatus(ngay, ngay2, allowedStatuses, pageable);
        } else if (mahd != null && !mahd.trim().isEmpty()) {
            // Tìm theo mã và chỉ lấy các trạng thái cho phép
            page = service.searchByMaAndStatus(mahd, allowedStatuses, pageable);
        } else {
            // Mặc định: chỉ lấy các trạng thái cho phép
            page = service.findByTrangThaiIn(allowedStatuses, pageable);
        }

        // Nếu page vẫn null (trường hợp lỗi), lấy danh sách mặc định
        if (page == null) {
            page = service.findByTrangThaiIn(allowedStatuses, pageable);
        }

        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        // Thống kê số lượng theo từng trạng thái (chỉ trong danh sách cho phép)
        model.addAttribute("totalChoXacNhan", service.countByTrangThai("Chờ xác nhận"));
        model.addAttribute("totalDaXacNhan", service.countByTrangThai("Đã xác nhận"));
        model.addAttribute("totalDangGiao", service.countByTrangThai("Đang giao"));
        model.addAttribute("totalHoanThanh", 0); // Không hiển thị

        model.addAttribute("tt", tt);

        // Lấy chi tiết hóa đơn nếu có mahd
        HoaDon hd = null;
        if (mahd != null && !mahd.trim().isEmpty()) {
            hd = service.findById(mahd);
            if (hd != null && allowedStatuses.contains(hd.getTrangThai())) {
                model.addAttribute("listsp", hoaDonChiTietService.findById(mahd));
            } else {
                model.addAttribute("listsp", List.of());
                hd = null; // Không hiển thị nếu không thuộc trạng thái cho phép
            }
        } else {
            model.addAttribute("listsp", List.of());
        }

        model.addAttribute("hd", hd);
        model.addAttribute("listhduy", service.findByTrangThai("Đã huỷ"));
        model.addAttribute("hoaDon", new HoaDon());

        return "donhang/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable String id,
                       @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {

        List<String> allowedStatuses = Arrays.asList("Chờ xác nhận", "Đã xác nhận", "Đang giao");

        HoaDon hd = service.findById(id);

        model.addAttribute("hoaDon", hd);
        model.addAttribute("hd", hd);

        if (hd != null && allowedStatuses.contains(hd.getTrangThai())) {
            model.addAttribute("listsp", hoaDonChiTietService.findById(id));
        } else {
            model.addAttribute("listsp", List.of());
        }

        Page<HoaDon> page = service.findByTrangThaiIn(allowedStatuses, pageable);

        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("listhduy", service.findByTrangThai("Đã huỷ"));
        model.addAttribute("activeMenu", "donhang");

        return "donhang/index";
    }

    @GetMapping("/suatt")
    public String suatt(@RequestParam(required = false) String mahd, Model model) {
        service.suatt(mahd);
        return "redirect:/donhang/index";
    }

    @GetMapping("/suattdg")
    public String suattdg(@RequestParam(required = false) String mahd, Model model) {
        service.suattdg(mahd);
        return "redirect:/donhang/index";
    }

    @GetMapping("/suattdgg")
    public String suattdgg(@RequestParam(required = false) String mahd, Model model) {
        service.suattdgg(mahd);
        return "redirect:/donhang/index";
    }

    @GetMapping("/api/detail")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@RequestParam("mahd") String maHoaDon) {
        Map<String, Object> result = new HashMap<>();
        try {
            HoaDon hd = service.findById(maHoaDon);
            if (hd != null) {
                result.put("success", true);
                result.put("maHoaDon", hd.getMaHoaDon());
                result.put("nhanVien", hd.getMaNhanVien() != null ? hd.getMaNhanVien().getHoTen() : "");
                result.put("khachHang", hd.getMaKhachHang() != null ?
                        hd.getMaKhachHang().getHoTen() + " - " + hd.getMaKhachHang().getSdt() : "Khách lẻ");
                result.put("tongTien", String.format("%,d", hd.getTongTien()));
                result.put("thanhToan", hd.getPhuongThucThanhToan());
                result.put("trangThai", hd.getTrangThai());
                result.put("loaiHoaDon", hd.getLoaiBan());
                result.put("ngayTao", hd.getNgayTao() != null ?
                        hd.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                result.put("ghiChu", hd.getGhiChu());
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy đơn hàng");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<Map<String, Object>> getProducts(@RequestParam("mahd") String maHoaDon) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietService.findById(maHoaDon);
            for (HoaDonChiTiet ct : chiTietList) {
                Map<String, Object> item = new HashMap<>();
                item.put("tenSanPham", ct.getSanPhamChiTiet().getSanPham().getTenSanPham());
                item.put("kichThuoc", ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc());
                item.put("mauSac", ct.getSanPhamChiTiet().getMauSac().getTenMauSac());
                item.put("soLuong", ct.getSoLuong());
                item.put("donGia", ct.getDonGia());
                // Tính giảm giá
                if (ct.getMaHoaDon() != null && ct.getMaHoaDon().getMaGiamGia() != null) {
                    item.put("giamGia", ct.getMaHoaDon().getMaGiamGia().getTenGiamGia() +
                            " (-" + String.format("%,d", ct.getMaHoaDon().getMaGiamGia().getGiaTriGiam()) + "₫)");
                } else {
                    item.put("giamGia", "Không áp dụng");
                }
                result.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}