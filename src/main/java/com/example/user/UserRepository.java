package com.example.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname); // 닉네임 중복 확인


}
