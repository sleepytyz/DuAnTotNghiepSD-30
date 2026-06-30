package com.example.th06876_java202.Controller;

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

    private final MauSacService mauSacService;

    public MauSacController(MauSacService mauSacService) {
        this.mauSacService = mauSacService;
    }

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<MauSac> pageData = mauSacService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listms", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        // Tạo mã tự động khi vào
        String generatedCode = mauSacService.generateMaMauSac();

        if (!model.containsAttribute("mausac")) {
            MauSac newMauSac = new MauSac();
            newMauSac.setMaMauSac(generatedCode);
            model.addAttribute("mausac", newMauSac);
        } else {
            MauSac existing = (MauSac) model.getAttribute("mausac");
            if (existing != null && (existing.getMaMauSac() == null || existing.getMaMauSac().isEmpty())) {
                existing.setMaMauSac(generatedCode);
            }
        }

        return "mausac/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("mausac") @Valid MauSac mauSac,
                      Errors errors,
                      Model model,
                      RedirectAttributes redirectAttributes) {

        String normalizedTen = mauSacService.normalizeTenMauSac(mauSac.getTenMauSac());
        mauSac.setTenMauSac(normalizedTen);

        if (errors.hasErrors()) {
            String newCode = mauSacService.generateMaMauSac();
            mauSac.setMaMauSac(newCode);

            Page<MauSac> pageData = mauSacService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listms", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            model.addAttribute("mausac", mauSac);
            return "mausac/index";
        }

        if (mauSacService.existsByTenMauSac(mauSac.getTenMauSac())) {
            // Tạo mã mới cho lần thử lại
            String newCode = mauSacService.generateMaMauSac();
            mauSac.setMaMauSac(newCode);

            redirectAttributes.addFlashAttribute("mess", "Màu sắc '" + mauSac.getTenMauSac() + "' đã tồn tại!");
            redirectAttributes.addFlashAttribute("mausac", mauSac);
            return "redirect:/mausac/index";
        }

        // Nếu mã chưa có, tạo mới
        if (mauSac.getMaMauSac() == null || mauSac.getMaMauSac().isEmpty()) {
            String newCode = mauSacService.generateMaMauSac();
            mauSac.setMaMauSac(newCode);
        }

        mauSac.setTrangThai(true);

        mauSacService.add(mauSac);
        redirectAttributes.addFlashAttribute("successMess",
                "Thêm màu sắc '" + mauSac.getTenMauSac() + "' (mã: " + mauSac.getMaMauSac() + ") thành công!");
        return "redirect:/mausac/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            MauSac dmsp = mauSacService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công cho '" + dmsp.getTenMauSac() + "'");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công cho '" + dmsp.getTenMauSac() + "'");
                }
            } else {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy màu sắc!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/mausac/index";
    }
}