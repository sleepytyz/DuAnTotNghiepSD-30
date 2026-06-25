package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.HoaDon;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/donhang")
public class DonHangController {

    @Autowired
    private HoaDonService service;

    private final HoaDonChiTietService hoaDonChiTietService;

    public DonHangController( HoaDonChiTietService hoaDonChiTietService ) {
        this.hoaDonChiTietService = hoaDonChiTietService;
    }

    @GetMapping("/donhang")
    public String hoaDon(Model model){
        model.addAttribute("pageTitle", "Hóa đơn");
        return "donhang/index";
    }

    @GetMapping("/index")
    public String index(
            @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Integer mahd,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) LocalDate ngay,
            @RequestParam(required = false) LocalDate ngay2,
            Model model) {

        model.addAttribute("activeMenu", "donhang");

        Page<HoaDon> page = service.getALLDH(pageable);

        if (tt != null && !tt.trim().isEmpty()) {
            page = service.findByTrangThai(tt, pageable);
        } else if (ngay != null || ngay2 != null) {
            page = service.searchByNgayTaodh(ngay, ngay2, pageable);
        } else if (mahd != null) {
            // ⚠️ KHÔNG dùng searchByMadh để thay list nữa
            // chỉ load chi tiết thôi
        }

        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("tt", tt);

        // 👉 CHỈ dùng mahd để lấy chi tiết
        HoaDon hd = null;
        if (mahd != null) {
            hd = service.findById(mahd).orElse(null);
            model.addAttribute("listsp", hoaDonChiTietService.findById(mahd));
        } else {
            model.addAttribute("listsp", List.of());
        }

        model.addAttribute("hd", hd);

        model.addAttribute("listhduy", service.getALLDHHUY());
        model.addAttribute("hoaDon", new HoaDon());

        return "donhang/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {

        HoaDon hd = service.findById(id).orElse(null);

        model.addAttribute("hoaDon", hd);
        model.addAttribute("hd", hd);

        if (hd != null) {
            model.addAttribute("listsp", hoaDonChiTietService.findById(id));
        } else {
            model.addAttribute("listsp", List.of());
        }

        Page<HoaDon> page = service.getALLDH(pageable);

        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("listhduy", service.getALLDHHUY());
        model.addAttribute("activeMenu", "donhang");

        return "donhang/index";
    }

    @GetMapping("/suatt")
    public String suatt(@RequestParam(required = false) Integer mahd, Model model) {
        service.suatt(mahd);
        return "redirect:/donhang/index";
    }

    @GetMapping("/suattdg")
    public String suattdg(@RequestParam(required = false) Integer mahd, Model model) {
        service.suattdg(mahd);
        return "redirect:/donhang/index";
    }

    @GetMapping("/suattdgg")
    public String suattdgg(@RequestParam(required = false) Integer mahd, Model model) {
        service.suattdgg(mahd);
        return "redirect:/donhang/index";
    }


}