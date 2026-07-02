package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Entity.HoaDonChiTiet;
import com.example.th06876_java202.Service.ExcelExportService;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService service;

    @Autowired
    private ExcelExportService excelExportService;

    private final HoaDonChiTietService hoaDonChiTietService;

    public HoaDonController(HoaDonChiTietService hoaDonChiTietService) {
        this.hoaDonChiTietService = hoaDonChiTietService;
    }

    @GetMapping("/index")
    public String index(
            @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String mahd,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay2,
            @RequestParam(required = false) String filterType,
            Model model) {

        model.addAttribute("activeMenu", "hoadon");

        // Danh sách trạng thái cho trang HÓA ĐƠN (khác với Đơn hàng)
        List<String> allowedStatuses = Arrays.asList("Đã thanh toán", "Đã giao", "Đã trả hàng", "Đã huỷ");

        // Xử lý lọc nhanh
        if (filterType != null && !filterType.isEmpty()) {
            LocalDate today = LocalDate.now();
            switch (filterType) {
                case "today":
                    ngay = today;
                    ngay2 = today;
                    break;
                case "yesterday":
                    ngay = today.minusDays(1);
                    ngay2 = today.minusDays(1);
                    break;
                case "week":
                    ngay = today.minusDays(7);
                    ngay2 = today;
                    break;
                case "month":
                    ngay = today.minusDays(30);
                    ngay2 = today;
                    break;
                case "thisMonth":
                    ngay = today.withDayOfMonth(1);
                    ngay2 = today;
                    break;
                default:
                    break;
            }
            model.addAttribute("filterType", filterType);
        }

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
            LocalDateTime startDateTime = ngay != null ? ngay.atStartOfDay() : null;
            LocalDateTime endDateTime = ngay2 != null ? ngay2.atTime(23, 59, 59) : null;
            page = service.searchByNgayTaodhAndStatus(startDateTime, endDateTime, allowedStatuses, pageable);
        } else {
            // Mặc định: chỉ lấy các trạng thái cho phép (Đã thanh toán, Đã giao, Đã trả hàng, Đã huỷ)
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
        model.addAttribute("tt", tt);
        model.addAttribute("ngay", ngay);
        model.addAttribute("ngay2", ngay2);

        // Thống kê số lượng theo từng trạng thái (chỉ trong danh sách cho phép)
        model.addAttribute("totalDaThanhToan", service.countByTrangThai("Đã thanh toán"));
        model.addAttribute("totalDaGiao", service.countByTrangThai("Đã giao"));
        model.addAttribute("totalDaTraHang", service.countByTrangThai("Đã trả hàng"));
        model.addAttribute("totalDaHuy", service.countByTrangThai("Đã huỷ"));

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
        model.addAttribute("hoaDon", new HoaDon());

        return "hoadon/index";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(required = false) String mahd,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay2,
            @RequestParam(required = false) String filterType) {

        try {
            System.out.println("========== EXPORT EXCEL HÓA ĐƠN ==========");
            System.out.println("mahd: [" + mahd + "]");
            System.out.println("tt: [" + tt + "]");
            System.out.println("ngay: [" + ngay + "]");
            System.out.println("ngay2: [" + ngay2 + "]");
            System.out.println("filterType: [" + filterType + "]");

            // Danh sách trạng thái cho trang HÓA ĐƠN
            List<String> allowedStatuses = Arrays.asList("Đã thanh toán", "Đã giao", "Đã trả hàng", "Đã huỷ");

            // Xử lý lọc nhanh
            if (filterType != null && !filterType.isEmpty()) {
                LocalDate today = LocalDate.now();
                switch (filterType) {
                    case "today":
                        ngay = today;
                        ngay2 = today;
                        break;
                    case "yesterday":
                        ngay = today.minusDays(1);
                        ngay2 = today.minusDays(1);
                        break;
                    case "week":
                        ngay = today.minusDays(7);
                        ngay2 = today;
                        break;
                    case "month":
                        ngay = today.minusDays(30);
                        ngay2 = today;
                        break;
                    case "thisMonth":
                        ngay = today.withDayOfMonth(1);
                        ngay2 = today;
                        break;
                    default:
                        break;
                }
            }

            List<HoaDon> hoaDonList;

            if (mahd != null && !mahd.trim().isEmpty()) {
                // Xuất chi tiết 1 hóa đơn
                List<HoaDonChiTiet> chiTietList = hoaDonChiTietService.findById(mahd);
                if (chiTietList != null && !chiTietList.isEmpty()) {
                    ByteArrayInputStream in = excelExportService.exportChiTietHoaDonToExcel(chiTietList);
                    if (in == null) {
                        return ResponseEntity.badRequest().build();
                    }

                    String fileName = "Chi_tiet_hoa_don_HD" + mahd + "_" +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=" + fileName);
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(new InputStreamResource(in));
                }

                HoaDon hd = service.findById(mahd);
                if (hd != null && allowedStatuses.contains(hd.getTrangThai())) {
                    hoaDonList = List.of(hd);
                } else {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                // Lọc theo điều kiện
                if (tt != null && !tt.trim().isEmpty() && allowedStatuses.contains(tt)) {
                    hoaDonList = service.findAllByTrangThai(tt);
                } else if (ngay != null || ngay2 != null) {
                    LocalDateTime startDateTime = ngay != null ? ngay.atStartOfDay() : null;
                    LocalDateTime endDateTime = ngay2 != null ? ngay2.atTime(23, 59, 59) : null;
                    hoaDonList = service.searchByNgayTaodhAndStatusList(startDateTime, endDateTime, allowedStatuses);
                } else {
                    hoaDonList = service.findByTrangThaiInList(allowedStatuses);
                }
            }

            if (hoaDonList == null || hoaDonList.isEmpty()) {
                System.out.println("⚠️ Không có dữ liệu để xuất!");
                return ResponseEntity.badRequest().build();
            }

            System.out.println("✅ Số lượng hóa đơn: " + hoaDonList.size());

            ByteArrayInputStream in = excelExportService.exportHoaDonToExcel(hoaDonList);

            if (in == null) {
                System.err.println("❌ InputStream bị null!");
                return ResponseEntity.badRequest().build();
            }

            String fileName = "Danh_sach_hoa_don_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi export Excel: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    // ===== CÁC HÀM CHUYỂN TRẠNG THÁI =====
    @GetMapping("/suatt")
    public String suatt(@RequestParam(required = false) String mahd) {
        service.suatt(mahd);
        return "redirect:/hoa-don/index";
    }

    @GetMapping("/suattdg")
    public String suattdg(@RequestParam(required = false) String mahd) {
        service.suattdg(mahd);
        return "redirect:/hoa-don/index";
    }

    @GetMapping("/suattdgg")
    public String suattdgg(@RequestParam(required = false) String mahd) {
        service.suattdgg(mahd);
        return "redirect:/hoa-don/index";
    }
}