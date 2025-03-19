package com.example.board;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FreeBoardService {

    private final FreeBoardRepository freeBoardRepository;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private final Path freeImagePath = Paths.get(System.getProperty("user.dir"), "uploads/free_images");

    public List<FreeBoard> getAllPosts() {
        return freeBoardRepository.findAll();
    }
    public FreeBoard getPostId(Long id) {
        return freeBoardRepository.findById(id).orElse(null);
    }


    @Transactional
    public void savePost(String title, String content, String username, String nickname, String boardType, List<MultipartFile> images) {
        FreeBoard freeBoard = new FreeBoard();
        freeBoard.setTitle(title);
        freeBoard.setContent(content);
        freeBoard.setUsername(username);
        freeBoard.setNickname(nickname);
        freeBoard.setBoardType(boardType);
        freeBoard.setCreatedAt(LocalDateTime.now());
        freeBoard.setUpdatedAt(LocalDateTime.now());

        // 게시글 저장
        freeBoardRepository.save(freeBoard);

        // 이미지가 있을 경우 처리
        if (images != null && !images.isEmpty()) {
            StringBuilder imagePaths = new StringBuilder();

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imagePath = saveFreeImage(freeBoard.getId(), image);
                        if (imagePaths.length() > 0) {
                            imagePaths.append(";");
                        }
                        imagePaths.append(imagePath);
                    } catch (IOException e) {
                        System.err.println("이미지 저장 중 오류 발생: " + e.getMessage());
                        throw new RuntimeException("이미지 저장에 실패했습니다.");
                    }
                }
            }

            // 이미지 경로를 하나의 String으로 결합하여 FreeBoard 객체에 저장
            if (imagePaths.length() > 0) {
                freeBoard.setFreeFileImgs(imagePaths.toString());
                freeBoardRepository.save(freeBoard); // 이미지 경로 업데이트
            }
        }
    }

    private String saveFreeImage(Long postId, MultipartFile image) throws IOException {
        // 파일 이름 설정
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        // 파일 크기 체크
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB 이하로 업로드 해주세요.");
        }

        // 자유게시판 이미지 업로드 디렉토리 확인 및 생성
        Path userUploadDir = freeImagePath.resolve(postId.toString());
        if (!Files.exists(userUploadDir)) {
            Files.createDirectories(userUploadDir); // 디렉토리 생성
        }

        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        Path imageFile = userUploadDir.resolve(fileName);

        // 이미지 파일 저장
        image.transferTo(imageFile.toFile());

        return "/uploads/free_images/" + postId + "/" + fileName;
    }


    public void deletePost(Long id) {
        freeBoardRepository.deleteById(id);
    }
}
