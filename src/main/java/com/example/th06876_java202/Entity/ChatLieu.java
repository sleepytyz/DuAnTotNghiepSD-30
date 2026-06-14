package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ChatLieu")
public class ChatLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChatLieu")
    private Integer maChatLieu;

    @NotBlank(message = "Không bỏ trống tên chất liệu")
    @Pattern( regexp = "^[\\p{L}\\d\\s]*$", message = "Chất liệu chỉ chứa chữ cái số và khoảng trắng")
    @Column(name = "TenChatLieu")
    private String tenChatLieu;

}
