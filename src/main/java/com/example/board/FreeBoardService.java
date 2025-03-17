package com.example.board;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FreeBoardService {

    private final FreeBoardRepository freeBoardRepository;

    public List<FreeBoard> getAllPosts() {

        return freeBoardRepository.findAll();
    }

    @Transactional
    public void savePost(String title, String content, String nickname, String boardType, List<MultipartFile> images) {

        FreeBoard freeBoard = new FreeBoard();
        freeBoard.setTitle(title);
        freeBoard.setContent(content);
        freeBoard.setNickname(nickname);
        freeBoard.setCreatedAt(LocalDateTime.now());
        freeBoard.setUpdatedAt(LocalDateTime.now());
        freeBoard.setBoardType(boardType);

        freeBoardRepository.save(freeBoard);

        // 이미지가 있을 경우 처리
        if (images != null && !images.isEmpty()) {
            StringBuilder imagePaths = new StringBuilder();

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imagePath = saveBoardImage(freeBoard.getId(), image);
                        if (imagePaths.length() > 0) {
                            imagePaths.append(";");
                        }
                        imagePaths.append(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 이미지 경로를 하나의 String으로 결합하여 FreeBoard 객체에 저장
            if (imagePaths.length() > 0) {
                freeBoard.setFreeFileImgs(imagePaths.toString());
                freeBoardRepository.save(freeBoard);  // 이미지 경로 업데이트
            }
        }
    }

    private String saveBoardImage(Long postId, MultipartFile image) throws IOException {
        String fileName = postId + "_" + image.getOriginalFilename();

        String uploadDir = "uploads/board_images/" + postId;

        // 디렉토리가 존재하지 않으면 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // 디렉토리 생성
        }

        // 파일 경로 설정
        File file = new File(directory, fileName);

        // 파일 저장
        image.transferTo(file);

        // 저장된 파일의 URL 또는 경로 반환 (웹에서 접근할 수 있는 경로로 반환)
        return "/uploads/board_images/" + postId + "/" + fileName;
    }

    public FreeBoard getPostId(Long id) {

        return freeBoardRepository.findById(id).orElse(null);
    }


}
