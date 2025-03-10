package com.example.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

}
