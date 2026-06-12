package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService service;

    @GetMapping("/hoa-don")
    public String hoaDon(Model model){
        model.addAttribute("pageTitle", "Hóa đơn");
        return "hoadon/index";
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            Model model) {

        model.addAttribute("activeMenu", "hoadon");

        if (trangThai != null && !trangThai.isEmpty()) {
            model.addAttribute("list",
                    service.findByTrangThai(trangThai));
        } else {
            model.addAttribute("list",
                    service.searchByMa(keyword));
        }

        model.addAttribute("hoaDon", new HoaDon());
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);

        return "hoadon/index";
    }

    @PostMapping("/add")
    public String add(HoaDon hoaDon){
        service.save(hoaDon);
        return "redirect:/hoa-don";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id){
        service.delete(id);
        return "redirect:/hoa-don";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        model.addAttribute("hoaDon", service.findById(id));
        model.addAttribute("list", service.getAll());
        return "hoadon/index";
    }

    @PostMapping("/update")
    public String update(HoaDon hoaDon){
        service.save(hoaDon);
        return "redirect:/hoa-don";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model){
        model.addAttribute("hoaDon", service.findById(id));
        model.addAttribute("list", service.getAll());
        return "hoadonct/detail";
    }

    @PostMapping("/cap-nhat-trang-thai")
    @ResponseBody
    public String capNhatTrangThai(
            @RequestParam Integer maHoaDon,
            @RequestParam String trangThai){

        HoaDon hd = service.findById(maHoaDon);

        hd.setTrangThai(trangThai);

        service.save(hd);

        return "OK";
    }

}