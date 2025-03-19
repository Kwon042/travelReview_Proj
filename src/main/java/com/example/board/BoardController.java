package com.example.board;

import com.example.user.SiteUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class BoardController {

    private final ReviewBoardService reviewBoardService;
    private final FreeBoardService freeBoardService;
    private final NoticeService noticeService;
    private final ReviewBoardRepository reviewBoardRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    // 글 저장하기
    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public String savePost(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(name = "region", required = false) String region,
                           @AuthenticationPrincipal SiteUser currentUser,
                           @RequestParam(name = "boardType") String boardType,
                           @RequestParam(name = "image", required = false) List<MultipartFile> images) {
        String username = currentUser.getUsername();
        String nickname = currentUser.getNickname();

        switch (boardType) {
            case "reviewBoard":
                reviewBoardService.savePost(title, content, region, username, nickname, boardType, images);
                break;
            case "freeBoard":
                freeBoardService.savePost(title, content, username, nickname, boardType, images, currentUser.getId());
                break;
            case "notice":
                noticeService.savePost(title, content, username, nickname, boardType, images, currentUser.getId());
                break;
            default:
                throw new IllegalArgumentException("Invalid board type: " + boardType);
        }

        try {
            if ("reviewBoard".equals(boardType)) {
                String encodedRegion = URLEncoder.encode(region, "UTF-8");
                return "redirect:/Boards/reviewBoard?region=" + encodedRegion;
            } else {
                return "redirect:/Boards/" + boardType;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "redirect:/Boards/reviewBoard";
        }
    }

    // 게시물 상세 페이지
    @GetMapping("/detail/{boardType}/{id}")
    public String detailPage(@PathVariable("boardType") String boardType,
                             @PathVariable("id") Long id,
                             @RequestParam(required = false) String region,
                             @AuthenticationPrincipal SiteUser currentUser,
                             Model model) {
        Object post;

        if (boardType == null || boardType.isEmpty()) {
            throw new IllegalArgumentException("Board type cannot be null or empty.");
        }

        switch (boardType) {
            case "reviewBoard":
                post = reviewBoardService.getPostId(id);
                break;
            case "freeBoard":
                post = freeBoardService.getPostId(id);
                break;
            case "notice":
                post = noticeService.getPostId(id);
                break;
            default:
                throw new IllegalArgumentException("Invalid board type: " + boardType);
        }
        String postUsername = null;

        if (post instanceof ReviewBoard) {
            postUsername = ((ReviewBoard) post).getUsername();
        } else if (post instanceof FreeBoard) {
            postUsername = ((FreeBoard) post).getUsername();
        } else if (post instanceof Notice) {
            postUsername = ((Notice) post).getUsername();
        } else {
            throw new IllegalArgumentException("Invalid post type.");
        }

        // 게시물 작성자 정보 모델에 추가
        model.addAttribute("boardType", boardType);
        model.addAttribute("region", region);
        model.addAttribute("post", post);

        model.addAttribute("currentUsername", currentUser.getUsername());

        return "Boards/detail";
    }

    // 게시글 수정
    @Transactional
    @PostMapping("/{boardType}/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> editPost(@PathVariable String boardType,
                                      @PathVariable Long id,
                                      @RequestParam(required = false) String title,
                                      @RequestParam(required = false) String content,
                                      @AuthenticationPrincipal SiteUser currentUser, Model model) {
        // 게시글을 가져옵니다.
        Object post = getPostById(boardType, id);

        // 게시글이 없는 경우
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found.");
        }

        model.addAttribute("post", post);
        model.addAttribute("boardType", boardType);

        String postUsername = getPostUsername(post);
        String nickname = currentUser.getNickname();

        // 로그인한 사용자가 작성자인지 확인
        if (!postUsername.equals(currentUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to edit this post.");
        }

        // 게시글 타입별로 수정 처리
        if (post instanceof ReviewBoard) {
            ReviewBoard reviewPost = (ReviewBoard) post;
            // 기존 데이터를 수정
            if (title != null) reviewPost.setTitle(title);
            if (content != null) reviewPost.setContent(content);
            reviewBoardRepository.save(reviewPost);
        } else if (post instanceof FreeBoard) {
            FreeBoard freePost = (FreeBoard) post;
            freePost.setId(id);
            if (title != null) freePost.setTitle(title);
            if (content != null) freePost.setContent(content);

            freePost.setUsername(currentUser.getUsername());
            freePost.setNickname(nickname);
            freeBoardRepository.save(freePost);
        } else if (post instanceof Notice) {
            Notice noticePost = (Notice) post;
            noticePost.setId(id);
            if (title != null) noticePost.setTitle(title);
            if (content != null) noticePost.setContent(content);

            noticePost.setUsername(currentUser.getUsername());
            noticePost.setNickname(nickname);
            noticeRepository.save(noticePost);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid board type.");
        }

        return ResponseEntity.ok().body(boardType + " post updated successfully");
    }

    @GetMapping("/{boardType}/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String showEditForm(@PathVariable String boardType,
                               @PathVariable Long id,
                               @AuthenticationPrincipal SiteUser currentUser, Model model) {
        // 게시글을 조회
        Object post = getPostById(boardType, id);

        if (post == null) {
            return "error/404";
        }

        String postUsername = getPostUsername(post);

        // 수정하려는 사용자가 게시글 작성자인지 확인
        if (!postUsername.equals(currentUser.getUsername())) {
            return "error/forbidden";
        }

        // 해당 게시판 타입에 맞는 데이터 전달
        if ("reviewBoard".equals(boardType)) {
            ReviewBoard reviewBoard = (ReviewBoard) post;
            model.addAttribute("post", reviewBoard);
            model.addAttribute("region", reviewBoard.getRegion());
        } else if ("freeBoard".equals(boardType)) {
            FreeBoard freeBoard = (FreeBoard) post;
            model.addAttribute("post", freeBoard);
        } else if ("notice".equals(boardType)) {
            Notice notice = (Notice) post;
            model.addAttribute("post", notice);
        }

        model.addAttribute("boardType", boardType);

        return "Boards/write";
    }

    // 게시글 삭제
    @DeleteMapping("/{boardType}/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable String boardType,
                                        @PathVariable Long id,
                                        @AuthenticationPrincipal SiteUser currentUser) {
        Object post;
        System.out.println("Received boardType: " + boardType); // 디버깅용 로그

        switch (boardType) {
            case "reviewBoard":
                ReviewBoard reviewPost = reviewBoardService.getPostId(id);
                if (reviewPost.getUsername().equals(currentUser.getUsername())) {
                    reviewBoardService.deletePost(id);
                    return ResponseEntity.ok().body("Post deleted successfully");
                }
                break;
            case "freeBoard":
                FreeBoard freePost = freeBoardService.getPostId(id);
                if (freePost.getUsername().equals(currentUser.getUsername())) {
                    freeBoardService.deletePost(id);
                    return ResponseEntity.ok().body("Post deleted successfully");
                }
                break;
            case "notice":
                Notice noticePost = noticeService.getPostId(id);
                if (noticePost.getUsername().equals(currentUser.getUsername())) {
                    noticeService.deletePost(id);
                    return ResponseEntity.ok().body("Post deleted successfully");
                }
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid board type");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this post.");
    }


    private String getPostUsername(Object post) {
        if (post instanceof ReviewBoard) {
            return ((ReviewBoard) post).getUsername();
        } else if (post instanceof FreeBoard) {
            return ((FreeBoard) post).getUsername();
        } else if (post instanceof Notice) {
            return ((Notice) post).getUsername();
        } else {
            throw new IllegalArgumentException("Invalid post type.");
        }
    }

    private Object getPostById(String boardType, Long id) {
        switch (boardType) {
            case "reviewBoard":
                return reviewBoardService.getPostId(id);
            case "freeBoard":
                return freeBoardService.getPostId(id);
            case "notice":
                return noticeService.getPostId(id);
            default:
                throw new IllegalArgumentException("Invalid board type: " + boardType);
        }
    }

}