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

    private String title; // 글제목
    private String nickname; // 작성자 닉네임
    private String content; // 내용

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시
    private Long hit; // 조회수

    @Column(name = "noticefile_img")
    private String noticeFileImg;

    private String noticeFileImgs; // 여러 이미지 경로를 저장할 필드


}
