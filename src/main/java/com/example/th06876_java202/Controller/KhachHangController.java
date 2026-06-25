package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DiaChi;
import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Service.DiaChiService;
import com.example.th06876_java202.Service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {
    private final KhachHangService khachHangService;

    @Autowired
    private DiaChiService diaChiService;

    @GetMapping("/hien-thi")
    public String khachHang(@PageableDefault(size = 10, page = 0, sort = "maKH",  direction = Sort.Direction.DESC)
                            Pageable pageable,
                            Model model) {
        model.addAttribute("activeMenu", "khachhang");

        Page<KhachHang> khachHangPage = khachHangService.getAllKhachHangPagin(pageable);

        model.addAttribute("khachHangPage", khachHangPage);
        model.addAttribute("khachHangs", khachHangPage.getContent());
        model.addAttribute("currentPage", khachHangPage.getNumber());
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", khachHangPage.getSize());

        model.addAttribute("showModal", false); // Mặc định false
        model.addAttribute("isEdit", false);  // Mặc định false

        model.addAttribute("kh", new KhachHang());
        return "khachhang/index";
    }

    @GetMapping("/edit/{maKH}")
    public String edit(@PathVariable Integer maKH,
                       @PageableDefault(size = 10, page = 0) Pageable pageable,
                       Model model) {

        model.addAttribute("activeMenu", "khachhang");

        Page<KhachHang> khachHangPage = khachHangService.getAllKhachHangPagin(pageable);

        model.addAttribute("khachHangPage", khachHangPage);
        model.addAttribute("khachHangs", khachHangPage.getContent());
        model.addAttribute("currentPage", khachHangPage.getNumber());
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", khachHangPage.getSize());

        KhachHang khachHang = khachHangService.getKhachHangById(maKH);

        List<DiaChi> dsDiaChi = diaChiService.findByKhachHang(maKH);

        khachHang.setDanhSachDiaChi(dsDiaChi);

        model.addAttribute("kh", khachHang);

        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);

        return "khachhang/add";
    }

    @DeleteMapping("/xoa-dia-chi/{id}")
    @ResponseBody
    public ResponseEntity<String> xoaDiaChi(@PathVariable Integer id) {

        diaChiService.delete(id);

        return ResponseEntity.ok("success");
    }

    @GetMapping("/lock/{maKH}")
    public String lock(@PathVariable Integer maKH) {
        khachHangService.lock(maKH);
        return "redirect:/khach-hang/hien-thi";
    }

    @GetMapping("/unlock/{maKH}")
    public String unlock(@PathVariable Integer maKH) {
        khachHangService.unlock(maKH);
        return "redirect:/khach-hang/hien-thi";
    }

    @GetMapping("/add-view")
    public String addView(Model model) {
        KhachHang kh = new KhachHang();
        kh.setDanhSachDiaChi(new ArrayList<>());
        model.addAttribute("kh", kh);
        return "khachhang/add";
    }
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("kh") KhachHang form, BindingResult result,
                      @RequestParam(value = "diaChiMacDinh", required = false) Integer indexMacDinh,
                       Model model) {

        khachHangService.validateKhachHang(form, result);

        if (result.hasErrors()) {
            model.addAttribute("kh", form);
            return "khachhang/add";
        }
        KhachHang khachHang;

        if (form.getMaKH() != null) {
            khachHang = khachHangService.getKhachHangById(form.getMaKH());
        } else {
            khachHang = new KhachHang();
            khachHang.setDanhSachDiaChi(new ArrayList<>());
        }
        khachHang.setHoTen(form.getHoTen());
        khachHang.setSdt(form.getSdt());
        khachHang.setEmail(form.getEmail());
        khachHang.setNgaySinh(form.getNgaySinh());
        khachHang.setGioiTinh(form.getGioiTinh());
        khachHang.setGhiChu(form.getGhiChu());

        List<DiaChi> list = form.getDanhSachDiaChi();

        if (list != null) {

            khachHang.getDanhSachDiaChi().clear();

            for (int i = 0; i < list.size(); i++) {
                DiaChi dc = list.get(i);

                dc.setKhachHang(khachHang);
                dc.setDiaChiMacDinh(indexMacDinh != null && i == indexMacDinh);

                khachHang.getDanhSachDiaChi().add(dc);
            }
        }

        khachHangService.save(khachHang);

        return "redirect:/khach-hang/hien-thi";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute KhachHang kh) {
        khachHangService.save(kh);
        return "redirect:/khach-hang/hien-thi";
    }

    @GetMapping("/locsdt")
    public String locsdt(@RequestParam("sdt") String sdt,
                         @PageableDefault(size = 10, sort = "maKH", direction = Sort.Direction.DESC)
                         Pageable pageable,
                         Model model) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return "redirect:/khach-hang/hien-thi";
        }
        Page<KhachHang> khachHangPage = khachHangService.findBySdt(sdt, pageable);

        model.addAttribute("activeMenu", "khachhang");
        model.addAttribute("khachHangPage", khachHangPage);
        model.addAttribute("khachHangs", khachHangPage.getContent());
        model.addAttribute("sdt", sdt);
        model.addAttribute("isFiltered", true);

        // Các biến phụ
        model.addAttribute("currentPage", khachHangPage.getNumber());
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("kh", new KhachHang());

        return "khachhang/index";
    }


}