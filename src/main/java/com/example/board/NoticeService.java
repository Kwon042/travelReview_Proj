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
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    @Transactional
    public void savePost(String title, String content, String nickname, String boardType, List<MultipartFile> images) {

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
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

    private String saveBoardImage(Long postId, MultipartFile image) throws IOException {
        // 이미지 저장 로직
        String fileName = postId + "_" + image.getOriginalFilename();
        String uploadDir = "uploads/notice_images/" + postId; // 게시글 ID별로 이미지 경로 설정

        // 디렉토리 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일 저장
        File file = new File(directory, fileName);
        image.transferTo(file);

        return "/uploads/notice_images/" + postId + "/" + fileName;
    }

    public Notice getPostId(Long id) {

        return noticeRepository.findById(id).orElse(null);
    }


}
