package com.example.board;

import com.example.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReviewBoardService {
    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;

    public List<ReviewBoard> getBoardByRegion(String region) {

        return reviewBoardRepository.findByRegion(region);
    }

    @Transactional
    public void savePost(String title, String content, String region, String nickname) {
        saveToAllBoards(title, content, region, nickname);
        saveToRegionalBoard(title, content, region, nickname);
    }

    void saveToAllBoards(String title, String content, String region, String nickname) {
        Optional<ReviewBoard> existingPost = reviewBoardRepository.findByRegionAndTitle("전체", title);

        if (existingPost.isPresent()) {
            System.out.println("Post already exists in '전체' board: " + title);
            return;
        }
        ReviewBoard allBoardPost = new ReviewBoard();
        allBoardPost.setTitle(title);
        allBoardPost.setContent(content);
        allBoardPost.setRegion("전체");  
        allBoardPost.setNickname(nickname);
        allBoardPost.setHit(0L);
        allBoardPost.setVoter(0L);
        allBoardPost.setCreatedAt(LocalDateTime.now());
        allBoardPost.setUpdatedAt(LocalDateTime.now());

        // 전체 게시판에 저장
        this.reviewBoardRepository.save(allBoardPost);
    }

    void saveToRegionalBoard(String title, String content, String region, String nickname) {
        ReviewBoard regionalPost = new ReviewBoard();
        regionalPost.setTitle(title);
        regionalPost.setContent(content);
        regionalPost.setRegion(region);
        regionalPost.setNickname(nickname);
        regionalPost.setHit(0L);
        regionalPost.setVoter(0L);
        regionalPost.setCreatedAt(LocalDateTime.now());
        regionalPost.setUpdatedAt(LocalDateTime.now());

        // 지역 게시판에 저장
        this.reviewBoardRepository.save(regionalPost);
    }

    public ReviewBoard getPostId(Long id) {
        return reviewBoardRepository.findById(id).orElse(null);
    }




}
