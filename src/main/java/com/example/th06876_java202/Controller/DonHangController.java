package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String index(@RequestParam(required = false) Integer mahd, Model model) {
        model.addAttribute("activeMenu", "hoadon");
        List<HoaDon> listhd = service.getALLDH();
        model.addAttribute("list", listhd);
        List<HoaDon> listhdhuy = service.getALLDHHUY();
        model.addAttribute("listhduy", listhdhuy);
        if (mahd != null) {
            HoaDon hd = service.findById(mahd).orElse(null);

            model.addAttribute("hd", hd);

            model.addAttribute(
                    "listsp",
                    hoaDonChiTietService.findById(mahd)
            );
        }
        model.addAttribute("hoaDon", new HoaDon());
        return "donhang/index";
    }

    @GetMapping("/locmahd")
    public String locmahd(@RequestParam("mahd") Integer  mahd ,Model model) {
        List<HoaDon> listhd = service.searchByMadh(mahd);
        model.addAttribute("list", listhd);
        List<HoaDon> listhdhuy = service.getALLDHHUY();
        model.addAttribute("listhduy", listhdhuy);
        return "donhang/index";
    }

    @GetMapping("/loctt")
    public String loctt(@RequestParam("tt") String tt ,Model model) {
        List<HoaDon> listhd = service.findByTrangThai(tt);
        model.addAttribute("list", listhd);
        List<HoaDon> listhdhuy = service.getALLDHHUY();
        model.addAttribute("listhduy", listhdhuy);
        return "donhang/index";
    }

    @GetMapping("/locngay")
    public String locngay(
            @RequestParam(required = false) LocalDate ngay,
            @RequestParam(required = false) LocalDate ngay2,
            Model model) {

            List<HoaDon> list =
                    service.searchByNgayTaodh(ngay, ngay2);
            model.addAttribute("list", list);
        List<HoaDon> listhdhuy = service.getALLDHHUY();
        model.addAttribute("listhduy", listhdhuy);
        return "donhang/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        model.addAttribute("hoaDon", service.findById(id));
        model.addAttribute("list", service.getALLDH());
        List<HoaDon> listhdhuy = service.getALLDHHUY();
        model.addAttribute("listhduy", listhdhuy);
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