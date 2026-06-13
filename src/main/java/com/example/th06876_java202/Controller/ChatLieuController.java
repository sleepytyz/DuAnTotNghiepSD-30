package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ChatLieu;
import com.example.th06876_java202.Service.ChatLieuService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/chatlieu")
public class ChatLieuController {

    private final ChatLieuService chatLieuService;

    public ChatLieuController(ChatLieuService chatLieuService) {
        this.chatLieuService = chatLieuService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        List<ChatLieu> listcl = chatLieuService.findAll();
        model.addAttribute("listcl", listcl);
        model.addAttribute("chatlieu", new ChatLieu());
        return "chatlieu/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("chatlieu")@Valid ChatLieu chatLieu, Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            List<ChatLieu> listcl = chatLieuService.findAll();
            model.addAttribute("listcl", listcl);
            return "chatlieu/index";
        }
        if (chatLieuService.existsByTenChatLieu(chatLieu.getTenChatLieu())) {
            redirectAttributes.addFlashAttribute("mess", "Chât liệu đã tồn tại");
            return "redirect:/chatlieu/index";
        }
        chatLieuService.add(chatLieu);
        return "redirect:/chatlieu/index";
    }

}
