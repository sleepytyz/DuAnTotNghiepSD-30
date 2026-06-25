package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.MauSac;
import com.example.th06876_java202.Service.MauSacService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mausac")
public class MauSacController {

    private final MauSacService maSacService;

    public MauSacController(MauSacService maSacService) {
        this.maSacService = maSacService;
    }

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<MauSac> pageData = maSacService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listms", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        if (!model.containsAttribute("mausac")) {
            model.addAttribute("mausac", new MauSac());
        }

        return "mausac/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("mausac") @Valid MauSac maSac, Errors errors, Model model, RedirectAttributes redirectAttributes) {

            String tenmoinee = maSac.getTenMauSac().trim();
            maSac.setTenMauSac(tenmoinee);

        if (errors.hasErrors()) {
            Page<MauSac> pageData = maSacService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listms", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            return "mausac/index";
        }

        if (maSacService.existbyten(maSac.getTenMauSac())) {
            redirectAttributes.addFlashAttribute("mess", "Màu sắc đã tồn tại");
            return "redirect:/mausac/index";
        }

        maSac.setTrangThai(true);

        maSacService.add(maSac);
        redirectAttributes.addFlashAttribute("successMess", "Thêm mới màu sắc thành công!");
        return "redirect:/mausac/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            MauSac dmsp = maSacService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công");
                }
            } else {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy danh mục sản phẩm!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/mausac/index";
    }
}