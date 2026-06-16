package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/staff/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {
    private final KhachHangService khachHangService;

    @GetMapping("/hien-thi")
    public String khachHang(@PageableDefault(size = 10, page = 0, direction = Sort.Direction.DESC)
                            Pageable pageable,
                            Model model) {
        model.addAttribute("activeMenu", "khachhang");

        Page<KhachHang> khachHangPage = khachHangService.getAllKhachHangPagin(pageable);

        model.addAttribute("khachHangPage", khachHangPage);
        model.addAttribute("khachHangs", khachHangPage.getContent());
        model.addAttribute("currentPage", khachHangPage.getNumber());
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", khachHangPage.getSize());

        model.addAttribute("showModal", false); // Mặc định false
        model.addAttribute("isEdit", false);  // Mặc định false

        model.addAttribute("kh", new KhachHang());
        return "khachhang/index";
    }

    @GetMapping("/edit/{maKH}")
    public String edit(@PathVariable Integer maKH,
                       @PageableDefault(size = 10, page = 0) Pageable pageable,
                       Model model) {

        model.addAttribute("activeMenu", "khachhang");

        Page<KhachHang> khachHangPage = khachHangService.getAllKhachHangPagin(pageable);

        model.addAttribute("khachHangPage", khachHangPage);
        model.addAttribute("khachHangs", khachHangPage.getContent());
        model.addAttribute("currentPage", khachHangPage.getNumber());
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", khachHangPage.getSize());

        KhachHang khachHang = khachHangService.getKhachHangById(maKH);
        model.addAttribute("kh", khachHang);

        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);

        return "khachhang/index";
    }

    @GetMapping("/lock/{maKH}")
    public String lock(@PathVariable Integer maKH) {
        khachHangService.lock(maKH);
        return "redirect:/staff/khach-hang/hien-thi";
    }

    @GetMapping("/unlock/{maKH}")
    public String unlock(@PathVariable Integer maKH) {
        khachHangService.unlock(maKH);
        return "redirect:/staff/khach-hang/hien-thi";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute KhachHang khachHang) {
        khachHang.setHangKhachHang("Mới");
        khachHang.setDiemTichLuy(0);
        khachHang.setNgayDangKy(LocalDate.now());
        khachHang.setTrangThai(true);
        khachHangService.save(khachHang);
        return "redirect:/staff/khach-hang/hien-thi";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute KhachHang kh) {
        khachHangService.save(kh);
        return "redirect:/staff/khach-hang/hien-thi";
    }

    // SỬA LẠI - THÊM CÁC BIẾN PHÂN TRANG
    @GetMapping("/locsdt")
    public String locsdt(@RequestParam("sdt") String sdt,
                         Model model) {
        List<KhachHang> listkh = khachHangService.findBySdt(sdt);

        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHangs", listkh);
        model.addAttribute("khachHangPage", null);
        // THÊM CÁC BIẾN PHÂN TRANG VỚI GIÁ TRỊ MẶC ĐỊNH
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("totalItems", listkh.size());
        model.addAttribute("size", 10);
        model.addAttribute("kh", new KhachHang());
        model.addAttribute("isFiltered", true);

        return "khachhang/index";
    }

    // SỬA LẠI - THÊM CÁC BIẾN PHÂN TRANG
    @GetMapping("/lochang")
    public String lochang(@RequestParam("hang") String hang,
                          Model model) {
        List<KhachHang> listkh = khachHangService.findByHangKH(hang);

        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHangs", listkh);
        model.addAttribute("khachHangPage", null);
        // THÊM CÁC BIẾN PHÂN TRANG VỚI GIÁ TRỊ MẶC ĐỊNH
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("totalItems", listkh.size());
        model.addAttribute("size", 10);
        model.addAttribute("kh", new KhachHang());
        model.addAttribute("isFiltered", true);

        return "khachhang/index";
    }
}