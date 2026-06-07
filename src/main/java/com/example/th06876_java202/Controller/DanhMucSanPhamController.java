package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Service.DanhMucSanPhamService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/danhmucsp")
public class DanhMucSanPhamController {

    private final DanhMucSanPhamService danhMucSanPhamService;

    public DanhMucSanPhamController( DanhMucSanPhamService danhMucSanPhamService ){
        this.danhMucSanPhamService = danhMucSanPhamService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("danhmuc", new DanhMucSanPham());
        return "danhmucsp/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        DanhMucSanPham dmsp = danhMucSanPhamService.findById(id).orElse(null);
        model.addAttribute("danhmuc", dmsp);
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listdmsp", listdmsp);
        return "danhmucsp/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("danhmuc")@Valid DanhMucSanPham dmsp, Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
            model.addAttribute("listdmsp", listdmsp);
            return "danhmucsp/index";
        }
        if (danhMucSanPhamService.ktraten(dmsp.getTenDanhMuc())){
            redirectAttributes.addFlashAttribute("mess", "Tên danh mục đã tồn tại");
        }
        danhMucSanPhamService.them(dmsp);
        return "redirect:/danhmucsp/index";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("danhmuc")@Valid DanhMucSanPham dmsp, Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
            model.addAttribute("listdmsp", listdmsp);
            return "danhmucsp/index";
        }
        if (danhMucSanPhamService.ktraten(dmsp.getTenDanhMuc())){
            redirectAttributes.addFlashAttribute("mess", "Tên danh mục đã tồn tại");
        }
        danhMucSanPhamService.them(dmsp);
        return "redirect:/danhmucsp/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        danhMucSanPhamService.updatett(id);
        return "redirect:/danhmucsp/index";
    }
}
