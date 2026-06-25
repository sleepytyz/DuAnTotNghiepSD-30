package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DanhMucSanPham;
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

        if (!model.containsAttribute("kichthuoc")) {
            model.addAttribute("kichthuoc", new KichThuoc());
        }
        return "kichthuoc/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("kichthuoc") @Valid KichThuoc kichThuoc,
                      Errors errors,
                      RedirectAttributes redirectAttributes,
                      Model model) {


            String tenm = kichThuoc.getTenKichThuoc().trim();
            kichThuoc.setTenKichThuoc(tenm);

        if (errors.hasErrors()) {
            Page<KichThuoc> pageData = kichThuocService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listk", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            return "kichthuoc/index";
        }

        if (kichThuocService.existsKichThuocByTenKichThuoc(kichThuoc.getTenKichThuoc())) {
            redirectAttributes.addFlashAttribute("mess", "Kích thước này đã tồn tại");
            return "redirect:/kichthuoc/index";
        }

        kichThuoc.setTrangThai(true);

        kichThuocService.add(kichThuoc);
        redirectAttributes.addFlashAttribute("successMess", "Thêm kích thước mới thành công!");
        return "redirect:/kichthuoc/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            KichThuoc dmsp = kichThuocService.doiTrangThai(id);

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
        return "redirect:/kichthuoc/index";
    }

}