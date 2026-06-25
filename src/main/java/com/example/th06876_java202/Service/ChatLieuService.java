package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.ChatLieu;
import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Repository.ChatLieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatLieuService {

    private final ChatLieuRepository chatLieuRepository;

    public ChatLieuService(ChatLieuRepository chatLieuRepository) {
        this.chatLieuRepository = chatLieuRepository;
    }

    public List<ChatLieu> findAll() {
        return chatLieuRepository.findAll();
    }

    public ChatLieu add(ChatLieu chatLieu) {
        return chatLieuRepository.save(chatLieu);
    }

    public Optional<ChatLieu> findById(Integer id) {
        return chatLieuRepository.findById(id);
    }

    public boolean existsByTenChatLieu(String tenChatLieu) {
        return chatLieuRepository.existsByTenChatLieu(tenChatLieu);
    }

    public ChatLieu doiTrangThai(Integer id) {
        Optional<ChatLieu> optional = chatLieuRepository.findById(id);
        if (optional.isPresent()) {
            ChatLieu dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return chatLieuRepository.save(dm);
        }
        return null;
    }

    public Page<ChatLieu> getallpage(Pageable pageable) {
        return chatLieuRepository.findAllByOrderByMaChatLieuDesc(pageable);
    }

}
