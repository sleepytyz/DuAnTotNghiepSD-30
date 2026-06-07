package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Entity.SanPhamHinhAnh;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import com.example.th06876_java202.Service.SanPhamHinhAnhService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/sanphamha")
public class SanPhamHinhAnhController {

    private final SanPhamHinhAnhService sanPhamHinhAnhService;
    private final SanPhamChiTietService sanPhamChiTietService;

    public SanPhamHinhAnhController(
            SanPhamHinhAnhService sanPhamHinhAnhService,
            SanPhamChiTietService sanPhamChiTietService) {

        this.sanPhamHinhAnhService = sanPhamHinhAnhService;
        this.sanPhamChiTietService = sanPhamChiTietService;
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] image(@PathVariable Integer id){
        return sanPhamHinhAnhService
                .findById(id)
                .orElseThrow()
                .getHinhAnh();
    }

    @GetMapping("/set-main/{id}")
    public String setMain(@PathVariable Integer id) {

        sanPhamHinhAnhService.setAnhChinh(id);

        return "redirect:/sanphamha/index";
    }

    @GetMapping("/index")
    public String index(Model model) {

        List<SanPhamHinhAnh> listAnh =
                sanPhamHinhAnhService.getAll();

        List<SanPhamChiTiet> listSPCT =
                sanPhamChiTietService.getDistinctSanPhamMau();

        model.addAttribute("listAnh", listAnh);
        model.addAttribute("listSPCT", listSPCT);
        model.addAttribute("sanPhamHinhAnh", new SanPhamHinhAnh());

        return "sanphamha/index";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam("file") MultipartFile file,
            @RequestParam("maSPCT") Integer maSPCT,
            @RequestParam(value = "laAnhChinh",
                    defaultValue = "false") Boolean laAnhChinh)
            throws IOException {

        SanPhamChiTiet spct =
                sanPhamChiTietService
                        .findbyId(maSPCT)
                        .orElseThrow();

        List<SanPhamChiTiet> dsSPCT =
                sanPhamChiTietService.getBySanPhamVaMau(
                        spct.getMaSanPham().getMaSanPham(),
                        spct.getMauSac());

        for(SanPhamChiTiet item : dsSPCT){

            SanPhamHinhAnh anh = new SanPhamHinhAnh();

            anh.setSanPhamChiTiet(item);
            anh.setHinhAnh(file.getBytes());
            anh.setLaAnhChinh(false);

            anh = sanPhamHinhAnhService.save(anh);

            if(laAnhChinh){
                sanPhamHinhAnhService.setAnhChinh(
                        anh.getMaHinhAnh());
            }
        }

        return "redirect:/sanphamha/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        sanPhamHinhAnhService.delete(id);

        return "redirect:/sanphamha/index";
    }
}