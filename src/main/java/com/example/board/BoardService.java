package com.example.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final ReviewBoardRepository reviewBoardRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final NoticeRepository noticeRepository;

    public Object getPostById(String boardType, Long id) {
        if ("reviewBoard".equals(boardType)) {
            return reviewBoardRepository.findById(id).orElse(null);
        } else if ("freeBoard".equals(boardType)) {
            return freeBoardRepository.findById(id).orElse(null);
        } else if ("notice".equals(boardType)) {
            return noticeRepository.findById(id).orElse(null);
        }
        return null;
    }
}
