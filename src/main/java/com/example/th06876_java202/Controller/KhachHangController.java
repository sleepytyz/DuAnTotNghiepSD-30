package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;


    @GetMapping("/hien-thi")
    public String khachHang(Model model) {
        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHang", khachHangService.getKhachHang());
        model.addAttribute("kh", new KhachHang());
        return "khachhang/index";
    }

    @GetMapping("/edit/{maKH}")
    public String edit(@PathVariable Integer maKH, Model model) {

        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHang", khachHangService.getKhachHang());
        model.addAttribute("kh", khachHangService.getKhachHangById(maKH));

        model.addAttribute("showModal", true);

        return "khachhang/index";
    }

    @GetMapping("/delete/{maKH}")
    public String delete(@PathVariable Integer maKH) {
        khachHangService.updatett(maKH);
        return "redirect:/khach-hang/hien-thi";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute KhachHang khachHang) {
        khachHang.setNgayDangKy(LocalDate.now());
        khachHangService.save(khachHang);
        return "redirect:/khach-hang/hien-thi";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute KhachHang kh) {
        khachHangService.save(kh);
        return "redirect:/khach-hang/hien-thi";
    }

    @GetMapping("/locsdt")
    public String locsdt(@RequestParam("sdt") String sdt,Model model) {
        List<KhachHang> listkh = khachHangService.findBySdt(sdt);
        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHang", listkh);
        model.addAttribute("kh", new KhachHang());
        return "khachhang/index";
    }

    @GetMapping("/lochang")
    public String lochang(@RequestParam("hang")String hang,Model model) {
        List<KhachHang> listkh = khachHangService.findByHangKH(hang);
        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHang", listkh);
        model.addAttribute("kh", new KhachHang());
        return "khachhang/index";
    }

}
