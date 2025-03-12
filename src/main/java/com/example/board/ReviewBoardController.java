package com.example.board;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Jakarta Servlet을 사용하는 경우

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class ReviewBoardController {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    private final ReviewBoardService reviewBoardService;
    private final ReviewBoardRepository reviewBoardRepository;

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

    @GetMapping("/reviewBoard") // 중복 제거
    public String showReviewBoard(@RequestParam(name = "region", required = false) String region, Model model) {
        if (region == null || region.isEmpty()) {
            region = "전체";
        }
        List<ReviewBoard> boards = reviewBoardService.getBoardByRegion(region);

        model.addAttribute("region", region);
        model.addAttribute("boards", boards);
        model.addAttribute("isAllBoard", "전체".equals(region)); // 지역이 '전체'인지 확인
        model.addAttribute("boardType", "reviewBoard"); // 게시판 유형 추가
        return "Boards/reviewBoard";
    }

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public String savePost(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(name = "region", required = true) String region,
                           @RequestParam(name = "nickname") String nickname) {
        reviewBoardService.savePost(title, content, region, nickname);
        try {
            String encodedRegion = URLEncoder.encode(region, "UTF-8");
            return "redirect:/Boards/reviewBoard?region=" + encodedRegion; // 인코딩 적용
        } catch (UnsupportedEncodingException e) {
            // 예외 처리 로직
            e.printStackTrace();
            return "redirect:/Boards/reviewBoard?region=전체"; // 안전한 기본값 설정
        }
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model) {
        ReviewBoard post = reviewBoardService.getPostId(id);
        model.addAttribute("post", post);
        return "Boards/detail";
    }


}