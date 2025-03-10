package com.example.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String UPLOAD_IMG = "path/to/upload/directory";

    @Transactional
    public SiteUser create(String username, String email, String password, String nickname) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);

        this.userRepository.save(user);
        return user;
    }

    public SiteUser authenticate(String username, String password) {
        Optional<SiteUser> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            SiteUser user = optionalUser.get();
            // 비밀번호 매칭 확인
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null; // 인증 실패 시 null 리턴
    }

    // 닉네임 수정하기
    public void updateNickname(Long userId, String newNickname) {
        SiteUser user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setNickname(newNickname);
            userRepository.save(user);
        }
    }

    // 파일 업로드
    public void uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        SiteUser user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // 파일 이름을 가져오기
            String originalFilename = file.getOriginalFilename();
            // 파일 경로 설정
            File uploadDir = new File(UPLOAD_IMG);
            if(!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            File imageFile = new File(uploadDir, originalFilename);

            // 파일을 지정된 경로에 저장
            file.transferTo(imageFile);

            // 저장할 이미지 URL 설정 (저장한 경로에 따라 조정)
            String imageUrl = "/uploads/" + originalFilename;
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);
        }
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public SiteUser getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
