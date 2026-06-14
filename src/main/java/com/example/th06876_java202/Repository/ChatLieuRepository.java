package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ChatLieu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    boolean existsByTenChatLieu(String tenChatLieu);
}
