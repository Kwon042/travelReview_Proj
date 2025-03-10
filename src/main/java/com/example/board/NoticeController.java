package com.example.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice")
    public String notice(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        model.addAttribute("boardType", "notice");
        return "/Boards/notice";
    }
}
