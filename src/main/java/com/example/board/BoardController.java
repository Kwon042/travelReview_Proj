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

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @GetMapping("/write")
    public String write(@RequestParam(value = "region", required = false) String region,
                        @RequestParam(value = "boardType", required = true) String boardType,
                        HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("boardType", boardType);

        // 전체 게시판인지 확인
        boolean isAllBoard = "reviewBoard".equals(boardType);
        model.addAttribute("isAllBoard", isAllBoard);

        model.addAttribute("region", region);

        return "Boards/write";
    }

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
                // 지역이 필요한 경우
                reviewBoardService.savePost(title, content, region, username, nickname, boardType, images);
                break;
            case "freeBoard":
                freeBoardService.savePost(title, content, username, nickname, boardType, images);
                break;
            case "notice":
                noticeService.savePost(title, content, username, nickname, boardType, images);
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

        // 로그 추가
        System.out.println("Current User Username: " + currentUser.getUsername());
        System.out.println("Post Author Username: " + (post instanceof ReviewBoard ? ((ReviewBoard) post).getUsername() : (post instanceof FreeBoard ? ((FreeBoard) post).getUsername() : ((Notice) post).getUsername())));


        return "Boards/detail";
    }

    @PostMapping("/{boardType}/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> editPost(@PathVariable String boardType,
                                      @PathVariable Long id,
                                      @RequestParam String title,
                                      @RequestParam String content,
                                      @AuthenticationPrincipal SiteUser currentUser, Model model) {
        Object post = getPostById(boardType, id);
        model.addAttribute("post", post);
        model.addAttribute("boardType", boardType);

        String postUsername = getPostUsername(post);
        String nickname = currentUser.getNickname();

        if (postUsername.equals(currentUser.getUsername())) {
            updatePost(post, title, content, ((ReviewBoard) post).getRegion(), currentUser.getUsername(), nickname, boardType, null);
            return ResponseEntity.ok().body(boardType + " post updated successfully");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to edit this post.");
    }

    private void updatePost(Object post, String title, String content, String region, String username, String nickname, String boardType, List<MultipartFile> images) {
        if (post instanceof ReviewBoard) {
            ReviewBoard reviewPost = (ReviewBoard) post;
            reviewPost.setTitle(title);
            reviewPost.setContent(content);
            reviewPost.setRegion(region);
            reviewPost.setUsername(username);
            reviewPost.setNickname(nickname);
            reviewBoardService.savePost(title, content, region, username, nickname, boardType, images);
        } else if (post instanceof FreeBoard) {
            FreeBoard freePost = (FreeBoard) post;
            freePost.setTitle(title);
            freePost.setContent(content);
            freePost.setUsername(username);
            freePost.setNickname(nickname);
            freeBoardService.savePost(title, content, username, nickname, boardType, images);
        } else if (post instanceof Notice) {
            Notice noticePost = (Notice) post;
            noticePost.setTitle(title);
            noticePost.setContent(content);
            noticePost.setUsername(username);
            noticePost.setNickname(nickname);
            noticeService.savePost(title, content, username, nickname, boardType, images);
        } else {
            throw new IllegalArgumentException("Invalid post type: " + post.getClass().getName());
        }
    }

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