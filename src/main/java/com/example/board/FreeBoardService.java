package com.example.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FreeBoardService {

    private final FreeBoardRepository freeBoardRepository;

    public List<FreeBoard> getAllPosts() {
        return freeBoardRepository.findAll();
    }

}
