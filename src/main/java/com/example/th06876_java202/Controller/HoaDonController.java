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
    public String index(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("activeMenu", "hoadon");
        model.addAttribute("list", service.searchByMa(keyword));
        model.addAttribute("hoaDon", new HoaDon());
        model.addAttribute("keyword", keyword); // Giữ lại từ khóa trên ô input sau khi tìm
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
}