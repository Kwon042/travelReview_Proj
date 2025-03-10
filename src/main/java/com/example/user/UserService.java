package com.example.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

}
