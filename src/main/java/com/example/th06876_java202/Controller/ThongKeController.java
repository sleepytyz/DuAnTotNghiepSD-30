package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ThongKeDoanhThuDTO;
import com.example.th06876_java202.Entity.ThongKeTheoThangDTO;
import com.example.th06876_java202.Entity.ThongKeTongQuanDTO;
import com.example.th06876_java202.Service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/thong-ke")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    @GetMapping("/doanh-thu")
    public String viewThongKeDoanhThu(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
            Model model) {

        if (startDate == null) {
            endDate = LocalDateTime.now();
            startDate = endDate.minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Thống kê theo ngày
        List<ThongKeDoanhThuDTO> thongKeNgay =
                thongKeService.thongKeDoanhThuTheoNgay(startDate, endDate);

        // Thống kê theo tháng
        List<ThongKeTheoThangDTO> thongKeThang =
                thongKeService.thongKeDoanhThuTheoThang(startDate, endDate);

        // Tổng quan
        ThongKeTongQuanDTO tongQuan = thongKeService.thongKeTongQuan();

        // Debug
        System.out.println("=== Controller Debug ===");
        System.out.println("thongKeNgay size: " + thongKeNgay.size());
        System.out.println("thongKeThang size: " + thongKeThang.size());
        System.out.println("tongQuan: " + tongQuan);

        model.addAttribute("thongKeNgay", thongKeNgay);
        model.addAttribute("thongKeThang", thongKeThang);
        model.addAttribute("tongQuan", tongQuan);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "thongke/index";
    }
}