package com.example.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notice_board")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;

    private String title;
    private String nickname;
    private String content;
    private String boardType;
    private String imageName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private Long hit;

    @Column(name = "noticefile_img")
    private String noticeFileImg;

    public String getImageName() {
        return noticeFileImg; // single image
    }

    private String noticeFileImgs; // 여러 이미지 경로를 저장할 필드


}
