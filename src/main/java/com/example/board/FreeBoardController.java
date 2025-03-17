package com.example.board;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;


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

    @GetMapping("/free/write")
    public String writeFree(@RequestParam(value = "boardType", required = true) String boardType,
                                  HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("boardType", boardType);

        return "Boards/write";
    }

    @PostMapping("/free/save")
    @PreAuthorize("isAuthenticated()")
    public String savePost(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(name = "nickname") String nickname,
                           @RequestParam(name = "image", required = false) List<MultipartFile> images) {
        freeBoardService.savePost(title, content, nickname, images);
        System.out.println("Redirecting to /Boards/freeBoard"); // Adding log statement


        return "redirect:/Boards/freeBoard";
    }

    @GetMapping("/free/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        FreeBoard post = freeBoardService.getPostId(id);

        model.addAttribute("post", post);

        return "Boards/detail";
    }
}
