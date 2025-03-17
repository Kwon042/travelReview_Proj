package com.example.board;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class NoticeController {

    private final NoticeService noticeService;

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @GetMapping("/notice")
    public String showNotice(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        model.addAttribute("boardType", "notice");
        return "/Boards/notice";
    }

    @GetMapping("/notice/write")
    public String writeNotice(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        model.addAttribute("boardType", "notice");

        return "Boards/write";
    }

    /*
    @PostMapping("/notice/save")
    @PreAuthorize("isAuthenticated()")
    public String saveNotice(@RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(name = "nickname") String nickname,
                             @RequestParam(name = "image", required = false) List<MultipartFile> images) {
        noticeService.savePost(title, content, nickname, images);

        return "redirect:/Boards/notice";
    }
     */

    @GetMapping("/notice/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        Notice post = noticeService.getPostId(id);

        model.addAttribute("post", post);

        return "Boards/detail";
    }
}
