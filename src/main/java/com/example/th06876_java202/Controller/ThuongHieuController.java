package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Service.ThuongHieuService;
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
@RequestMapping("/thuonghieu")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<ThuongHieu> pageData = thuongHieuService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listth", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        String generatedCode = thuongHieuService.generateMaThuongHieu();

        if (!model.containsAttribute("thuonghieu")) {
            ThuongHieu newThuongHieu = new ThuongHieu();
            newThuongHieu.setMaThuongHieu(generatedCode);
            model.addAttribute("thuonghieu", newThuongHieu);
        } else {
            ThuongHieu existing = (ThuongHieu) model.getAttribute("thuonghieu");
            if (existing != null && (existing.getMaThuongHieu() == null || existing.getMaThuongHieu().isEmpty())) {
                existing.setMaThuongHieu(generatedCode);
            }
        }

        return "thuonghieu/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("thuonghieu") @Valid ThuongHieu thuongHieu,
                      Errors errors,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        String normalizedTen = thuongHieuService.normalizeTenThuongHieu(thuongHieu.getTenThuongHieu());
        thuongHieu.setTenThuongHieu(normalizedTen);

        System.out.println("=== ADD THUONG HIEU ===");
        System.out.println("Ten nhap: " + thuongHieu.getTenThuongHieu());
        System.out.println("Errors: " + errors.hasErrors());

        if (errors.hasErrors()) {
            String newCode = thuongHieuService.generateMaThuongHieu();
            thuongHieu.setMaThuongHieu(newCode);

            Page<ThuongHieu> pageData = thuongHieuService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listth", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            model.addAttribute("thuonghieu", thuongHieu);
            return "thuonghieu/index";
        }

        if (thuongHieuService.ktraten(thuongHieu.getTenThuongHieu())) {
            String newCode = thuongHieuService.generateMaThuongHieu();
            thuongHieu.setMaThuongHieu(newCode);

            System.out.println("⚠Tên đã tồn tại: " + thuongHieu.getTenThuongHieu());
            redirectAttributes.addFlashAttribute("mess", "Thương hiệu '" + thuongHieu.getTenThuongHieu() + "' đã tồn tại!");
            redirectAttributes.addFlashAttribute("thuonghieu", thuongHieu);
            return "redirect:/thuonghieu/index";
        }

        if (thuongHieu.getMaThuongHieu() == null || thuongHieu.getMaThuongHieu().isEmpty()) {
            String newCode = thuongHieuService.generateMaThuongHieu();
            thuongHieu.setMaThuongHieu(newCode);
        }

        thuongHieu.setTrangThai(true);

        thuongHieuService.them(thuongHieu);
        System.out.println("Thêm thành công: " + thuongHieu.getTenThuongHieu() + " - " + thuongHieu.getMaThuongHieu());
        redirectAttributes.addFlashAttribute("successMess",
                "Thêm thương hiệu '" + thuongHieu.getTenThuongHieu() + "' (mã: " + thuongHieu.getMaThuongHieu() + ") thành công!");
        return "redirect:/thuonghieu/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            ThuongHieu dmsp = thuongHieuService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công cho '" + dmsp.getTenThuongHieu() + "'");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công cho '" + dmsp.getTenThuongHieu() + "'");
                }
            } else {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy thương hiệu!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/thuonghieu/index";
    }
}