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
    private Long id; // 번호

    private String title; // 글제목
    private String nickname; // 작성자 닉네임
    private String content; // 내용

    private String region; // 지역 클릭

    // 객체가 처음 저장할 때만 값을 설정하도록, 이후에는 수정하지 않기 위해 false
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시
    private Long hit; // 조회수
    private Long voter; // 좋아요 수

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
}
