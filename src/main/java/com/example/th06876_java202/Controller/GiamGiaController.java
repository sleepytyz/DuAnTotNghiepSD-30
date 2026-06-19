package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Service.GiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/giamgia")
public class GiamGiaController {

    @Autowired
    private GiamGiaService giamGiaService;

    @GetMapping("/index")
    public String index(Model model) {
        List<GiamGia> list = giamGiaService.getGiamGia();
        model.addAttribute("list", list);
        model.addAttribute("giamGia", new GiamGia());
        return "giamgia/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("giamGia")@Valid GiamGia giamGia, Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            List<GiamGia> list = giamGiaService.getGiamGia();
            model.addAttribute("list", list);
            model.addAttribute("showModal", true);
            return "giamgia/index";
        }
        if (giamGiaService.existsTenGiamGia(giamGia.getTenGiamGia())){
            redirectAttributes.addFlashAttribute("mess", "Tên chương trình đã tồn tại");
            return "redirect:/giamgia/index";
        }
        giamGiaService.save(giamGia);
        return "redirect:/giamgia/index";

    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        giamGiaService.suatt(id);
        return "redirect:/giamgia/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        GiamGia gg = giamGiaService.getGiamGiaById(id).orElse(null);

        model.addAttribute("giamGia", gg);
        model.addAttribute("list", giamGiaService.getGiamGia());
        model.addAttribute("showModal", true);
        return "giamgia/index";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("giamGia") @Valid GiamGia giamGia,
                         Errors errors,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        GiamGia old = giamGiaService
                .getGiamGiaById(giamGia.getMaGiamGia())
                .orElse(null);

        if(old != null && "Hoạt động".equals(old.getTrangThai())){
            redirectAttributes.addFlashAttribute(
                    "mess",
                    "Chương trình đang hoạt động nên không được chỉnh sửa!"
            );
            return "redirect:/giamgia/index";
        }

        if(errors.hasErrors()){
            model.addAttribute("list", giamGiaService.getGiamGia());
            model.addAttribute("showModal", true);
            return "giamgia/index";
        }

        giamGiaService.save(giamGia);
        return "redirect:/giamgia/index";
    }

    @GetMapping("loctt")
    public String loctt(@RequestParam("tt") String tt,Model model) {
        List<GiamGia> list = giamGiaService.loctt(tt);
        model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
        return "giamgia/index";
    }

    @GetMapping("loclg")
    public String loctloai(@RequestParam("lg") String tt,Model model) {
        List<GiamGia> list = giamGiaService.loclg(tt);
        model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
        return "giamgia/index";
    }

    @GetMapping("locten")
    public String locten(@RequestParam("ten") String tt,Model model) {
        List<GiamGia> list = giamGiaService.timkiem(tt);
        model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
        return "giamgia/index";
    }

    @GetMapping("locngay")
    public String loctt(@RequestParam("ngaybdau")LocalDateTime ngay1, @RequestParam("ngaykthuc")LocalDateTime ngay2 ,  Model model) {
        List<GiamGia> list = giamGiaService.locng(ngay1,ngay2);
        model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
        return "giamgia/index";
    }


}
