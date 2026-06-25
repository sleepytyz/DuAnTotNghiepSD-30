package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.KieuGiay;
import com.example.th06876_java202.Service.KieuGiayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/kieugiay")
public class KieuGiayController {

    @Autowired
    private KieuGiayService kieuGiayService;

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<KieuGiay> pageData = kieuGiayService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listkg", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements()); // Tổng số bản ghi kiểu giày

        if (!model.containsAttribute("kieugiay")) {
            model.addAttribute("kieugiay", new KieuGiay());
        }
        return "kieugiay/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("kieugiay") @Valid KieuGiay kieuGiay,
                      Errors errors,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        if (kieuGiay.getTenKieuGiay() != null) {
            kieuGiay.setTenKieuGiay(kieuGiay.getTenKieuGiay().trim());
        }

        if (errors.hasErrors()) {
            Page<KieuGiay> pageData = kieuGiayService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listkg", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            return "kieugiay/index";
        }

        kieuGiay.setTrangThai(true);

        kieuGiayService.them(kieuGiay);
        redirectAttributes.addFlashAttribute("successMess", "Thêm kiểu giày thành công!");
        return "redirect:/kieugiay/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            KieuGiay dmsp = kieuGiayService.doiTrangThai(id);

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
        return "redirect:/kieugiay/index";
    }
}