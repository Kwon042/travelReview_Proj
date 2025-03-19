package com.example.board;

import com.example.user.UserService;
import com.example.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static groovyjarjarantlr4.v4.gui.GraphicsSupport.saveImage;

@RequiredArgsConstructor
@Service
public class ReviewBoardService {
    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB (5 * 1024 * 1024 바이트)

    public String saveBoardImage(Long postId, MultipartFile file) throws IOException {
        // 파일 확장자 및 크기 검사
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (!Arrays.asList("jpg", "jpeg", "png").contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB 이하로 업로드 해주세요.");
        }

        // 게시글 이미지 경로 생성
        Path boardImagePath = Paths.get("uploads/board_images", String.valueOf(postId));
        if (!Files.exists(boardImagePath)) {
            Files.createDirectories(boardImagePath); // 게시판 이미지 디렉토리 생성
        }

        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        Path imageFile = boardImagePath.resolve(fileName); // 파일 경로 설정

        // 파일 저장
        file.transferTo(imageFile.toFile());
        String imagePath = "/uploads/board_images/" + postId + "/" + fileName;
        return imagePath;
    }

    @Transactional
    public void savePost(String title, String content, String region, String nickname, String boardType, List<MultipartFile> images) {

        saveToAllBoards(title, content, region, nickname, boardType);
        saveToRegionalBoard(title, content, region, nickname, boardType);
        // 지역 게시판의 글을 찾아서 이미지 저장
        Optional<ReviewBoard> regionalPost = reviewBoardRepository.findByRegionAndTitle(region, title);

        if (regionalPost.isPresent() && images != null && !images.isEmpty()) {
            ReviewBoard post = regionalPost.get();
            // 이미지 경로를 저장할 StringBuilder 생성
            StringBuilder imagePaths = new StringBuilder();

            for (MultipartFile image : images) {
                if (!image.isEmpty() && post.getId() != null) {
                    try {
                        String imagePath = saveBoardImage(post.getId(), image);  // 게시글 이미지를 저장하고 경로 반환
                        if (imagePaths.length() > 0) {
                            imagePaths.append(";");  // 경로들을 세미콜론으로 구분
                        }
                        imagePaths.append(imagePath);  // 이미지 경로를 추가
                    } catch (IOException e) {
                        e.printStackTrace();  // 예외 발생 시 처리
                    }
                }
            }
            // 여러 이미지 경로를 하나의 String으로 결합하여 ReviewBoard 객체에 설정
            post.setReviewFileImgs(imagePaths.toString());
            reviewBoardRepository.save(post);
        }
    }

    void saveToAllBoards(String title, String content, String region, String nickname, String boardType) {
        // "전체" 게시판에 동일한 제목과 내용의 글이 있는지 확인
        Optional<ReviewBoard> existingPost = reviewBoardRepository.findByRegionAndTitle("전체", title);

        // "전체" 게시판에 동일한 제목과 내용의 글이 없다면 새로 저장
        if (existingPost.isEmpty()) {
            ReviewBoard allBoardPost = new ReviewBoard();
            allBoardPost.setTitle(title);
            allBoardPost.setContent(content);
            allBoardPost.setRegion("전체");
            allBoardPost.setNickname(nickname);
            allBoardPost.setBoardType(boardType);
            allBoardPost.setHit(0L);
            allBoardPost.setVoter(0L);
            allBoardPost.setCreatedAt(LocalDateTime.now());
            allBoardPost.setUpdatedAt(LocalDateTime.now());

            this.reviewBoardRepository.save(allBoardPost);
        }
    }

    void saveToRegionalBoard(String title, String content, String region, String nickname, String boardType) {
        ReviewBoard regionalPost = new ReviewBoard();

        regionalPost.setTitle(title);
        regionalPost.setContent(content);
        regionalPost.setRegion(region);
        regionalPost.setNickname(nickname);
        regionalPost.setBoardType(boardType);
        regionalPost.setHit(0L);
        regionalPost.setVoter(0L);
        regionalPost.setCreatedAt(LocalDateTime.now());
        regionalPost.setUpdatedAt(LocalDateTime.now());

        this.reviewBoardRepository.save(regionalPost);
    }

    public ReviewBoard getPostId(Long id) {
        return reviewBoardRepository.findById(id).orElse(null);
    }

    public List<ReviewBoard> getBoardByRegion(String region) {

        return reviewBoardRepository.findByRegion(region);
    }

    public List<ReviewBoard> getAllBoards() {
        return reviewBoardRepository.findAll();
    }
}