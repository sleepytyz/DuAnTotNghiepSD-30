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
import java.util.Optional;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService service;

    private final HoaDonChiTietService hoaDonChiTietService;

    public HoaDonController( HoaDonChiTietService hoaDonChiTietService ) {
        this.hoaDonChiTietService = hoaDonChiTietService;
    }

    @GetMapping("/hoa-don")
    public String hoaDon(Model model){
        model.addAttribute("pageTitle", "Hóa đơn");
        return "hoadon/index";
    }

    @GetMapping("/index")
    public String index(
            @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Integer mahd,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) LocalDate ngay,
            @RequestParam(required = false) LocalDate ngay2,
            Model model) {

        model.addAttribute("activeMenu", "hoadon");

        Page<HoaDon> page = service.getHoaDonKhac(pageable);

        if (tt != null && !tt.trim().isEmpty()) {
            page = service.findByTrangThai(tt, pageable);
        } else if (ngay != null || ngay2 != null) {
            page = service.searchByNgayTao(ngay, ngay2, pageable);
        }

        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());

        HoaDon hd = null;
        if (mahd != null) {
            hd = service.findById(mahd).orElse(null);
        }

        model.addAttribute("hd", hd);

        if (hd != null) {
            model.addAttribute("listsp", hoaDonChiTietService.findById(mahd));
        } else {
            model.addAttribute("listsp", List.of());
        }

        model.addAttribute("hoaDon", new HoaDon());

        return "hoadon/index";
    }


    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Integer id,
            @PageableDefault(size = 5, sort = "maHoaDon", direction = Sort.Direction.DESC) Pageable pageable,
            Model model){

        model.addAttribute("hoaDon", service.findById(id).orElse(new HoaDon()));

        Page<HoaDon> page = service.getallpage(pageable);

        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("activeMenu", "hoadon");

        return "hoadon/index";
    }


    @PostMapping("/cap-nhat-trang-thai")
    @ResponseBody
    public String capNhatTrangThai(
            @RequestParam Integer maHoaDon,
            @RequestParam String trangThai){

        HoaDon hd = service.findById(maHoaDon).get();

        hd.setTrangThai(trangThai);

        service.save(hd);

        return "OK";
    }

}