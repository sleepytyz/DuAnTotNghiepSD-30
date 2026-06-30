package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ChatLieu;
import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Service.ChatLieuService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chatlieu")
public class ChatLieuController {

    private final ChatLieuService chatLieuService;

    public ChatLieuController(ChatLieuService chatLieuService) {
        this.chatLieuService = chatLieuService;
    }

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<ChatLieu> pageData = chatLieuService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listcl", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        if (!model.containsAttribute("chatlieu")) {
            model.addAttribute("chatlieu", new ChatLieu());
        }

        return "chatlieu/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("chatlieu") @Valid ChatLieu chatLieu,
                      Errors errors,
                      Model model,
                      RedirectAttributes redirectAttributes) {
            String tenmoine = chatLieu.getTenChatLieu().trim();
            chatLieu.setTenChatLieu(tenmoine);
        if (errors.hasErrors()) {
            Page<ChatLieu> pageData = chatLieuService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listcl", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            return "chatlieu/index";
        }

        if (chatLieuService.existsByTenChatLieu(chatLieu.getTenChatLieu())) {
            redirectAttributes.addFlashAttribute("mess", "Chất liệu đã tồn tại!");
            return "redirect:/chatlieu/index";
        }

        chatLieu.setTrangThai(true);

        chatLieuService.add(chatLieu);
        redirectAttributes.addFlashAttribute("successMess", "Thêm chất liệu thành công!"); // Có thể dùng hiển thị thông báo thành công
        return "redirect:/chatlieu/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            ChatLieu dmsp = chatLieuService.doiTrangThai(id);

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
        return "redirect:/chatlieu/index";
    }

}