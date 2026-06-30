package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.KichThuoc;
import com.example.th06876_java202.Service.KichThuocService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/kichthuoc")
public class KichThuocController {

    private final KichThuocService kichThuocService;

    public KichThuocController(KichThuocService kichThuocService) {
        this.kichThuocService = kichThuocService;
    }

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<KichThuoc> pageData = kichThuocService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listk", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        // Tạo mã tự động khi vào index
        String generatedCode = kichThuocService.generateMaKichThuoc();

        if (!model.containsAttribute("kichthuoc")) {
            KichThuoc newKichThuoc = new KichThuoc();
            newKichThuoc.setMaKichThuoc(generatedCode);
            model.addAttribute("kichthuoc", newKichThuoc);
        } else {
            KichThuoc existing = (KichThuoc) model.getAttribute("kichthuoc");
            if (existing != null && (existing.getMaKichThuoc() == null || existing.getMaKichThuoc().isEmpty())) {
                existing.setMaKichThuoc(generatedCode);
            }
        }

        return "kichthuoc/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("kichthuoc") @Valid KichThuoc kichThuoc,
                      Errors errors,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        // Chuẩn hóa tên: loại bỏ khoảng trắng thừa
        String normalizedTen = kichThuocService.normalizeTenKichThuoc(kichThuoc.getTenKichThuoc());
        kichThuoc.setTenKichThuoc(normalizedTen);

        System.out.println("=== ADD KICH THUOC ===");
        System.out.println("Ten nhap: " + kichThuoc.getTenKichThuoc());
        System.out.println("Errors: " + errors.hasErrors());

        if (errors.hasErrors()) {
            // Tạo mã mới khi có lỗi
            String newCode = kichThuocService.generateMaKichThuoc();
            kichThuoc.setMaKichThuoc(newCode);

            Page<KichThuoc> pageData = kichThuocService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listk", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            model.addAttribute("kichthuoc", kichThuoc);
            return "kichthuoc/index";
        }

        // Kiểm tra tên đã tồn tại (đã xử lý khoảng trắng)
        if (kichThuocService.existsByTenKichThuoc(kichThuoc.getTenKichThuoc())) {
            // Tạo mã mới cho lần thử lại
            String newCode = kichThuocService.generateMaKichThuoc();
            kichThuoc.setMaKichThuoc(newCode);

            System.out.println("Tên đã tồn tại: " + kichThuoc.getTenKichThuoc());
            redirectAttributes.addFlashAttribute("mess", "Kích thước '" + kichThuoc.getTenKichThuoc() + "' đã tồn tại!");
            redirectAttributes.addFlashAttribute("kichthuoc", kichThuoc);
            return "redirect:/kichthuoc/index";
        }

        if (kichThuoc.getMaKichThuoc() == null || kichThuoc.getMaKichThuoc().isEmpty()) {
            String newCode = kichThuocService.generateMaKichThuoc();
            kichThuoc.setMaKichThuoc(newCode);
        }

        kichThuoc.setTrangThai(true);

        kichThuocService.add(kichThuoc);
        System.out.println("Thêm thành công: " + kichThuoc.getTenKichThuoc() + " - " + kichThuoc.getMaKichThuoc());
        redirectAttributes.addFlashAttribute("successMess",
                "Thêm kích thước '" + kichThuoc.getTenKichThuoc() + "' (mã: " + kichThuoc.getMaKichThuoc() + ") thành công!");
        return "redirect:/kichthuoc/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            KichThuoc dmsp = kichThuocService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công cho '" + dmsp.getTenKichThuoc() + "'");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công cho '" + dmsp.getTenKichThuoc() + "'");
                }
            } else {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy kích thước!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/kichthuoc/index";
    }
}