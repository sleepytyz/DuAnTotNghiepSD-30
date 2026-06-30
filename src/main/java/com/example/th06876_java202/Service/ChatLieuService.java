package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.ChatLieu;
import com.example.th06876_java202.Repository.ChatLieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ChatLieuService {

    private final ChatLieuRepository chatLieuRepository;
    private final Random random = new Random();

    public ChatLieuService(ChatLieuRepository chatLieuRepository) {
        this.chatLieuRepository = chatLieuRepository;
    }

    public List<ChatLieu> findAll() {
        return chatLieuRepository.findAll();
    }

    public ChatLieu add(ChatLieu chatLieu) {
        return chatLieuRepository.save(chatLieu);
    }

    public Optional<ChatLieu> findById(String id) {
        return chatLieuRepository.findById(id);
    }

    /**
     * Tạo mã chất liệu tự động: CL + 4 số ngẫu nhiên
     */
    public String generateMaChatLieu() {
        String code;
        boolean exists;
        do {
            int randomNumber = 1000 + random.nextInt(9000); // Tạo số từ 1000-9999
            code = "CL" + randomNumber;
            exists = chatLieuRepository.existsById(code);
        } while (exists);
        return code;
    }

    /**
     * Chuẩn hóa tên: loại bỏ khoảng trắng thừa
     * - Xóa khoảng trắng đầu và cuối
     * - Giữa các từ chỉ 1 khoảng trắng
     * - Viết hoa chữ cái đầu mỗi từ
     */
    public String normalizeTenChatLieu(String ten) {
        if (ten == null) return "";

        // 1. Xóa khoảng trắng đầu và cuối
        ten = ten.trim();

        // 2. Giữa các từ chỉ 1 khoảng trắng
        ten = ten.replaceAll("\\s+", " ");

        // 3. Viết hoa chữ cái đầu mỗi từ
        String[] words = ten.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Chuẩn hóa tên để so sánh (không viết hoa, chỉ loại bỏ khoảng trắng thừa)
     */
    private String normalizeForCompare(String ten) {
        if (ten == null) return "";
        // 1. Xóa khoảng trắng đầu và cuối
        ten = ten.trim();
        // 2. Giữa các từ chỉ 1 khoảng trắng
        ten = ten.replaceAll("\\s+", " ");
        return ten;
    }

    public boolean existsByTenChatLieu(String tenChatLieu) {
        if (tenChatLieu == null) return false;

        String normalizedInput = normalizeForCompare(tenChatLieu);

        // Lấy tất cả chất liệu và kiểm tra từng cái
        List<ChatLieu> all = chatLieuRepository.findAll();
        for (ChatLieu cl : all) {
            String existingName = normalizeForCompare(cl.getTenChatLieu());
            if (existingName.equalsIgnoreCase(normalizedInput)) {
                return true;
            }
        }
        return false;
    }

    public ChatLieu doiTrangThai(String id) {
        Optional<ChatLieu> optional = chatLieuRepository.findById(id);
        if (optional.isPresent()) {
            ChatLieu dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return chatLieuRepository.save(dm);
        }
        return null;
    }

    public Page<ChatLieu> getallpage(Pageable pageable) {
        return chatLieuRepository.findAllByOrderByNgayTaoDesc(pageable);
    }
}