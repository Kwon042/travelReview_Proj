package com.example.board;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class ReviewBoardController {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    private final ReviewBoardService reviewBoardService;

    @GetMapping("/reviewBoard")
    public String showReviewBoard(@RequestParam(name = "region", required = false) String region, Model model) {
        if (region == null || region.isEmpty()) {
            region = "전체";
        }
        List<ReviewBoard> boards = reviewBoardService.getBoardByRegion(region);

        // 각 게시판의 이미지 URL을 model에 추가
        for (ReviewBoard board : boards) {
            if (board.getReviewFileImg() != null && !board.getReviewFileImg().isEmpty()) {
                // 이미지 경로가 존재하면
                String imagePath = "/uploads/review_images/" + board.getReviewFileImg();
                model.addAttribute("imagePath_" + board.getId(), imagePath);
            }
        }

        model.addAttribute("region", region);
        model.addAttribute("boards", boards);
        model.addAttribute("isAllBoard", "전체".equals(region)); // 지역이 '전체'인지 확인
        model.addAttribute("boardType", "reviewBoard"); // 게시판 유형 추가
        return "Boards/reviewBoard";
    }

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

}