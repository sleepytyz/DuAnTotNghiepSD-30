package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ChiTietDotGiamGia;
import com.example.th06876_java202.Repository.DotGiamGiaRepo;
import com.example.th06876_java202.Repository.SanPhamRepository;
import com.example.th06876_java202.Service.ChiTietDotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chi-tiet-dot-giam-gia")
public class ChiTietDotGiamGiaCon {

    @Autowired
    private ChiTietDotGiamGiaService service;

    @Autowired
    private DotGiamGiaRepo dotGiamGiaRepo;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping("/hien-thi")
    public String hienThi(Model model) {

        model.addAttribute("activeMenu", "ctdotgiamgia");

        model.addAttribute("listCTDGG", service.getAll());

        model.addAttribute("listDGG",
                dotGiamGiaRepo.findAll());

        model.addAttribute("listSP",
                sanPhamRepository.findAll());

        return "chitietdotgiamgia/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Integer id){

        service.delete(id);

        return "redirect:/chi-tiet-dot-giam-gia/hien-thi";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam Integer maGiamGia,
            @RequestParam Integer maSanPham) {

        if (!service.exists(maGiamGia, maSanPham)) {

            ChiTietDotGiamGia ct =
                    new ChiTietDotGiamGia();

            ct.setDotGiamGia(
                    dotGiamGiaRepo.findById(maGiamGia)
                            .orElse(null));

            ct.setSanPham(
                    sanPhamRepository.findById(maSanPham)
                            .orElse(null));

            service.save(ct);
        }

        return "redirect:/chi-tiet-dot-giam-gia/hien-thi";
    }
}
