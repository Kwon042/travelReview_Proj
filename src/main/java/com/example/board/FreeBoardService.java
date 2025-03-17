package com.example.board;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FreeBoardService {

    private final FreeBoardRepository freeBoardRepository;

    public List<FreeBoard> getAllPosts() {

        return freeBoardRepository.findAll();
    }

    @Transactional
    public void savePost(String title, String content, String nickname, List<MultipartFile> images) {

        FreeBoard freeBoard = new FreeBoard();
        freeBoard.setTitle(title);
        freeBoard.setContent(content);
        freeBoard.setNickname(nickname);
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
        // 이미지 저장 로직을 작성해야 합니다 (예: 파일 시스템이나 클라우드 저장소에 이미지 저장)
        // 여기서는 예시로 간단하게 파일 경로만 반환하는 예시입니다.

        String fileName = postId + "_" + image.getOriginalFilename();
        File file = new File("/path/to/images/" + fileName);
        image.transferTo(file);

        return "/images/" + fileName;
    }



}
