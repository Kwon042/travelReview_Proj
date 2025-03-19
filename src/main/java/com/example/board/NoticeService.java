package com.example.board;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private final Path noticeImagePath = Paths.get(System.getProperty("user.dir"), "uploads/notice_images");


    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    @Transactional
    public void savePost(String title, String content, String username, String nickname, String boardType, List<MultipartFile> images) {

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setUsername(username);
        notice.setNickname(nickname);
        notice.setBoardType(boardType);
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());

        noticeRepository.save(notice);

        // 이미지가 있을 경우 처리
        if (images != null && !images.isEmpty()) {
            StringBuilder imagePaths = new StringBuilder();

            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        String uploadDir = "uploads/notice/";
                        File uploadDirectory = new File(uploadDir);
                        if (!uploadDirectory.exists()) {
                            uploadDirectory.mkdirs(); // 디렉토리 생성
                        }
                        String filePath = uploadDir + file.getOriginalFilename();
                        file.transferTo(new File(filePath)); // 파일 저장
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 이미지 경로를 하나의 String으로 결합하여 Notice 객체에 저장
            if (imagePaths.length() > 0) {
                notice.setNoticeFileImgs(imagePaths.toString()); // 이미지 경로를 Notice 객체에 설정
            }
            noticeRepository.save(notice);
        }
    }

    private String saveNoticeImage(Long noticeId, MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        // 파일 크기 체크
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB 이하로 업로드 해주세요.");
        }

        // 공지사항 이미지 업로드 디렉토리 확인 및 생성
        Path userUploadDir = noticeImagePath.resolve(noticeId.toString());
        if (!Files.exists(userUploadDir)) {
            Files.createDirectories(userUploadDir); // 디렉토리 생성
        }

        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        Path imageFile = userUploadDir.resolve(fileName);

        // 이미지 파일 저장
        image.transferTo(imageFile.toFile());

        return "/uploads/notice_images/" + noticeId + "/" + fileName;
    }

    public Notice getPostId(Long id) {

        return noticeRepository.findById(id).orElse(null);
    }


    public void deletePost(Long id) {
        noticeRepository.deleteById(id);
    }
}
