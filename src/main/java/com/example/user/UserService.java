package com.example.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Path profileImagePath = Paths.get(System.getProperty("user.dir"), "uploads/profile_images");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB (5 * 1024 * 1024 바이트)

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

    // 닉네임 수정
    public void updateNickname(Long userId, String newNickname) {
        // 중복 체크
        String duplicateMessage = checkFieldDuplicate("nickname", newNickname);
        if (duplicateMessage != null) {
            throw new IllegalArgumentException(duplicateMessage);
        }

        SiteUser user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setNickname(newNickname);
        userRepository.save(user);
    }

    // 이메일 수정
    public void updateEmail(Long userId, String newEmail) {
        // 중복 체크
        String duplicateMessage = checkFieldDuplicate("email", newEmail);
        if (duplicateMessage != null) {
            throw new IllegalArgumentException(duplicateMessage);
        }

        SiteUser user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    // 중복 체크 공통 메서드
    private String checkFieldDuplicate(String field, String value) {
        if ("nickname".equals(field)) {
            boolean nicknameExists = isNicknameAlreadyRegistered(value);
            if (nicknameExists) {
                throw new IllegalArgumentException("이미 등록된 닉네임입니다.");
            }
        } else if ("email".equals(field)) {
            boolean emailExists = isEmailAlreadyRegistered(value);
            if (emailExists) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }
        return null;
    }

    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        try {
            SiteUser user = userRepository.findById(userId).orElseThrow(() ->
                    new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("파일 크기가 너무 큽니다.");
            }

            // 프로필 이미지 업로드 디렉토리 확인 및 생성
            Path profileImagePath = Paths.get(System.getProperty("user.dir"), "uploads/profile_images/" + userId);
            if (!Files.exists(profileImagePath)) {
                try {
                    Files.createDirectories(profileImagePath);
                } catch (IOException e) {
                    throw new RuntimeException("업로드 디렉토리 생성 오류: " + e.getMessage());
                }
            }

            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path imageFilePath = profileImagePath.resolve(fileName);
            file.transferTo(imageFilePath.toFile());

            // 이미지 URL을 사용자에게 저장
            String imageUrl = "/uploads/profile_images/" + userId + "/" + fileName;
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);

            return imageUrl;
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public SiteUser getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public void updateUser(SiteUser user) {
        userRepository.save(user);
    }

    public boolean isEmailAlreadyRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isNicknameAlreadyRegistered(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
