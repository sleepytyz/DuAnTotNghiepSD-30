package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;

//    @GetMapping("/test")
//    @ResponseBody
//    public String test() {
//        return "OK";
//    }

//    @GetMapping("/hien-thi")
//    @ResponseBody
//    public String khachHang() {
//        return "Hien thi OK";
//    }

    @GetMapping("/hien-thi")
    public String khachHang(Model model) {
        model.addAttribute("khachHang", khachHangService.getKhachHang());
        model.addAttribute("kh", new KhachHang());
        return "khach-hang";
    }

    @GetMapping("/edit/{maKH}")
    public String edit(@PathVariable Integer maKH, Model model) {
        model.addAttribute("khachHang", khachHangService.getKhachHang());
        model.addAttribute("kh", khachHangService.getKhachHangById(maKH));
        return "khach-hang";
    }

    @GetMapping("/delete/{maKH}")
    public String delete(@PathVariable Integer maKH) {
        khachHangService.delete(maKH);
        return "redirect:/khach-hang/hien-thi";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute KhachHang khachHang) {
        khachHang.setMaKH(null);
        khachHangService.save(khachHang);
        return "redirect:/khach-hang/hien-thi";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute KhachHang kh) {
        khachHangService.save(kh);
        return "redirect:/khach-hang/hien-thi";
    }

}
