package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ChatLieu;
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

        // Tạo mã tự động khi vào index
        String generatedCode = chatLieuService.generateMaChatLieu();

        if (!model.containsAttribute("chatlieu")) {
            ChatLieu newChatLieu = new ChatLieu();
            newChatLieu.setMaChatLieu(generatedCode);
            model.addAttribute("chatlieu", newChatLieu);
        } else {
            ChatLieu existing = (ChatLieu) model.getAttribute("chatlieu");
            if (existing != null && (existing.getMaChatLieu() == null || existing.getMaChatLieu().isEmpty())) {
                existing.setMaChatLieu(generatedCode);
            }
        }

        return "chatlieu/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("chatlieu") @Valid ChatLieu chatLieu,
                      Errors errors,
                      Model model,
                      RedirectAttributes redirectAttributes) {

        // Chuẩn hóa tên (loại bỏ khoảng trắng thừa, viết hoa chữ cái đầu)
        String normalizedTen = chatLieuService.normalizeTenChatLieu(chatLieu.getTenChatLieu());
        chatLieu.setTenChatLieu(normalizedTen);

        if (errors.hasErrors()) {
            // Tạo mã mới khi có lỗi
            String newCode = chatLieuService.generateMaChatLieu();
            chatLieu.setMaChatLieu(newCode);

            Page<ChatLieu> pageData = chatLieuService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listcl", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            model.addAttribute("chatlieu", chatLieu);
            return "chatlieu/index";
        }

        // Kiểm tra tên đã tồn tại (đã xử lý khoảng trắng và hoa thường ở Service)
        if (chatLieuService.existsByTenChatLieu(chatLieu.getTenChatLieu())) {
            // Tạo mã mới cho lần thử lại
            String newCode = chatLieuService.generateMaChatLieu();
            chatLieu.setMaChatLieu(newCode);

            redirectAttributes.addFlashAttribute("mess", "Chất liệu '" + chatLieu.getTenChatLieu() + "' đã tồn tại!");
            redirectAttributes.addFlashAttribute("chatlieu", chatLieu);
            return "redirect:/chatlieu/index";
        }

        // Nếu mã chưa có, tạo mới
        if (chatLieu.getMaChatLieu() == null || chatLieu.getMaChatLieu().isEmpty()) {
            String newCode = chatLieuService.generateMaChatLieu();
            chatLieu.setMaChatLieu(newCode);
        }

        chatLieu.setTrangThai(true);

        chatLieuService.add(chatLieu);
        redirectAttributes.addFlashAttribute("successMess",
                " Thêm chất liệu '" + chatLieu.getTenChatLieu() + "' (mã: " + chatLieu.getMaChatLieu() + ") thành công!");
        return "redirect:/chatlieu/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            ChatLieu dmsp = chatLieuService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công cho '" + dmsp.getTenChatLieu() + "'");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công cho '" + dmsp.getTenChatLieu() + "'");
                }
            } else {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy chất liệu!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/chatlieu/index";
    }
}