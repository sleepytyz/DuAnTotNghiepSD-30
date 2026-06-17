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
    public String index(@RequestParam(required = false) Integer mahd, Model model) {
        model.addAttribute("activeMenu", "hoadon");
        List<HoaDon> listhd = service.getAll();
        model.addAttribute("list", listhd);
        if (mahd != null) {
            HoaDon hd = service.findById(mahd).orElse(null);

            model.addAttribute("hd", hd);

            model.addAttribute(
                    "listsp",
                    hoaDonChiTietService.findById(mahd)
            );
        }
        model.addAttribute("hoaDon", new HoaDon());
        return "hoadon/index";
    }

    @GetMapping("/locmahd")
    public String locmahd(@RequestParam("mahd") Integer  mahd ,Model model) {
        List<HoaDon> listhd = service.searchByMa(mahd);
        model.addAttribute("list", listhd);
        return "hoadon/index";
    }

    @GetMapping("/loctt")
    public String loctt(@RequestParam("tt") String tt ,Model model) {
        List<HoaDon> listhd = service.findByTrangThai(tt);
        model.addAttribute("list", listhd);
        return "hoadon/index";
    }

    @GetMapping("/locngay")
    public String locngay(
            @RequestParam(required = false) LocalDate ngay,
            @RequestParam(required = false) LocalDate ngay2,
            Model model) {

        List<HoaDon> list =
                service.searchByNgayTao(ngay, ngay2);
        model.addAttribute("list", list);
        return "hoadon/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        model.addAttribute("hoaDon", service.findById(id));
        model.addAttribute("list", service.getAll());
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