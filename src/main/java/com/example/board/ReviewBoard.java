package com.example.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "review_board")
public class ReviewBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String nickname;
    private String content;

    private String region;

    // 객체가 처음 저장할 때만 값을 설정하도록, 이후에는 수정하지 않기 위해 false
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private Long hit;
    private Long voter;

    // PrePersist 메소드 하나로 모든 초기화를 처리 (timestamp)
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.hit == null) {
            this.hit = 0L;
        }
        if (this.voter == null) {
            this.voter = 0L;
        }
    }

    @Column(name = "reviewfile_img")
    private String reviewFileImg;

    private String reviewFileImgs; // 여러 이미지 경로를 저장할 필드

    public String getImageName() {
        return reviewFileImg;
    }

    public Long getId() {
        return id;
    }
}
