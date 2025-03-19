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
@Table(name = "free_board")
public class FreeBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String nickname;
    private String content;
    private String imageName;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Long hit;
    private Long voter;

    @Column(name = "freefile_img")
    private String freeFileImg;

    public String getImageName() {
        return freeFileImg; // single image
    }

    private String freeFileImgs; // 여러 이미지 경로를 저장할 필드

    private String boardType;


}
