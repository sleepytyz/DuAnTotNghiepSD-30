package com.example.th06876_java202.Controller;

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
        model.addAttribute("totalItems", pageData.getTotalElements());

        String generatedCode = kieuGiayService.generateMaKieuGiay();

        if (!model.containsAttribute("kieugiay")) {
            KieuGiay newKieuGiay = new KieuGiay();
            newKieuGiay.setMaKieuGiay(generatedCode);
            model.addAttribute("kieugiay", newKieuGiay);
        } else {
            KieuGiay existing = (KieuGiay) model.getAttribute("kieugiay");
            if (existing != null && (existing.getMaKieuGiay() == null || existing.getMaKieuGiay().isEmpty())) {
                existing.setMaKieuGiay(generatedCode);
            }
        }

        return "kieugiay/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("kieugiay") @Valid KieuGiay kieuGiay,
                      Errors errors,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        String normalizedTen = kieuGiayService.normalizeTenKieuGiay(kieuGiay.getTenKieuGiay());
        kieuGiay.setTenKieuGiay(normalizedTen);

        System.out.println("=== ADD KIEU GIAY ===");
        System.out.println("Ten nhap: " + kieuGiay.getTenKieuGiay());
        System.out.println("Errors: " + errors.hasErrors());

        if (errors.hasErrors()) {
            String newCode = kieuGiayService.generateMaKieuGiay();
            kieuGiay.setMaKieuGiay(newCode);

            Page<KieuGiay> pageData = kieuGiayService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listkg", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            model.addAttribute("kieugiay", kieuGiay);
            return "kieugiay/index";
        }

        if (kieuGiayService.existsByTenKieuGiay(kieuGiay.getTenKieuGiay())) {
            String newCode = kieuGiayService.generateMaKieuGiay();
            kieuGiay.setMaKieuGiay(newCode);

            System.out.println("Tên đã tồn tại: " + kieuGiay.getTenKieuGiay());
            redirectAttributes.addFlashAttribute("mess", "Kiểu giày '" + kieuGiay.getTenKieuGiay() + "' đã tồn tại!");
            redirectAttributes.addFlashAttribute("kieugiay", kieuGiay);
            return "redirect:/kieugiay/index";
        }
        if (kieuGiay.getMaKieuGiay() == null || kieuGiay.getMaKieuGiay().isEmpty()) {
            String newCode = kieuGiayService.generateMaKieuGiay();
            kieuGiay.setMaKieuGiay(newCode);
        }

        kieuGiay.setTrangThai(true);

        kieuGiayService.them(kieuGiay);
        System.out.println("Thêm thành công: " + kieuGiay.getTenKieuGiay() + " - " + kieuGiay.getMaKieuGiay());
        redirectAttributes.addFlashAttribute("successMess",
                "Thêm kiểu giày '" + kieuGiay.getTenKieuGiay() + "' (mã: " + kieuGiay.getMaKieuGiay() + ") thành công!");
        return "redirect:/kieugiay/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            KieuGiay dmsp = kieuGiayService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công cho '" + dmsp.getTenKieuGiay() + "'");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công cho '" + dmsp.getTenKieuGiay() + "'");
                }
            } else {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy kiểu giày!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/kieugiay/index";
    }
}