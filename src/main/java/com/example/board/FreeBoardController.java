package com.example.board;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class FreeBoardController {

    private final FreeBoardService freeBoardService;

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @GetMapping("/freeBoard")
    public String showfreeBoard(Model model) {
        model.addAttribute("posts", freeBoardService.getAllPosts());
        model.addAttribute("boardType", "freeBoard");
        return "/Boards/freeBoard";
    }

    @GetMapping("/write")
    public String write(@RequestParam(value = "region", required = false) String region,
                        @RequestParam(value = "boardType", required = true) String boardType,
                        HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("boardType", boardType);

        // freeBoard에 맞는 데이터 처리
        model.addAttribute("region", region);

        return "Boards/write";  // 공통 템플릿 반환
    }



}
