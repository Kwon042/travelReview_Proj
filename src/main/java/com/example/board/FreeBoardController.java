package com.example.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@RequiredArgsConstructor
@Controller
@RequestMapping("/Boards")
public class FreeBoardController {

    private final FreeBoardService freeBoardService;

    @GetMapping("/freeBoard")
    public String freeBoard(Model model) {
        model.addAttribute("posts", freeBoardService.getAllPosts());
        model.addAttribute("boardType", "freeBoard");
        return "/Boards/freeBoard";
    }

}
