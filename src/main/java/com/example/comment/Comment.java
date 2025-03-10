package com.example.comment;

import com.example.board.FreeBoard;
import com.example.board.ReviewBoard;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 댓글 ID

    @ManyToOne
    @JoinColumn(name = "review_id")
    private ReviewBoard review; // 리뷰 게시판의 댓글 ID

    @ManyToOne
    @JoinColumn(name = "board_id")
    private FreeBoard board; // 자유 게시판 댓글 ID

    private String nickname; // 회원닉네임
    private String content; // 댓글내용

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시

    private Long voter;
}