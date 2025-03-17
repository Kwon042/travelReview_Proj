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
    private Long id; // 번호

    private String title; // 글제목
    private String nickname; // 회원닉네임
    private String content; // 내용

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시

    private Long hit; // 조회수
    private Long voter; // 찜수

    @Column(name = "freefile_img")
    private String freeFileImg;

    private String freeFileImgs; // 여러 이미지 경로를 저장할 필드


}
