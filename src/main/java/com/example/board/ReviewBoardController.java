package com.example.board;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Jakarta Servlet을 사용하는 경우

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class ReviewBoardController {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    private final ReviewBoardService reviewBoardService;
    private final ReviewBoardRepository reviewBoardRepository;

    @GetMapping("/reviewBoard")
    public String showReviewBoard(@RequestParam(name = "region", required = false) String region, Model model) {
        if (region == null || region.isEmpty()) {
            region = "전체"; // 기본값
        }
        List<ReviewBoard> boards = reviewBoardService.getBoardByRegion(region);
        model.addAttribute("boards", boards);
        model.addAttribute("boardType", "reviewBoard");
        return "Boards/reviewBoard";
    }

    @GetMapping("/write")
    public String write(@RequestParam(value = "region", required = false) String region, HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        if (region == null || region.isEmpty()) {
            model.addAttribute("region", null);
        } else {
            model.addAttribute("region", region);
        }
        return "Boards/write";
    }

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근 가능
    public String savePost(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(name = "region", required = false, defaultValue = "전체") String region,
                           @RequestParam(name = "nickname") String nickname) {
        System.out.println("Received region: " + region);  // region 값이 제대로 들어오는지 확인
        reviewBoardService.savePost(title, content, region, nickname);
        return "redirect:/Boards/reviewBoard";
    }

    @GetMapping("/Boards/reviewBoard")
    public String getBoardList(@RequestParam(name = "region", required = false, defaultValue = "전체") String region, Model model) {
        List<ReviewBoard> boardList;

        if ("전체".equals(region)) {
            boardList = reviewBoardRepository.findByRegion("전체");
        } else {
            boardList = reviewBoardRepository.findByRegion(region);
        }

        model.addAttribute("boardList", boardList);
        return "Boards/reviewBoard";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model) {
        ReviewBoard post = reviewBoardService.getPostId(id);
        model.addAttribute("post", post);
        return "Boards/detail";
    }


}
