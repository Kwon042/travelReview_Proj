package com.example.user;

import com.example.board.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final NoticeService noticeService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String signup(@ModelAttribute UserCreateForm userCreateForm) {
        return "/user/signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        try {
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1(), userCreateForm.getNickname());
        }catch(DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        }catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "/user/login_form";
    }

    @GetMapping("/mypage")
    public String getMypage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/user/login";
        }
        SiteUser user = (SiteUser) authentication.getPrincipal();
        model.addAttribute("user", user);

        return "user/mypage";
    }

    @GetMapping("/mypage/edit")
    public String editUserInfo(@AuthenticationPrincipal SiteUser siteUser, Model model) {
        model.addAttribute("user", siteUser);
        return "user/mypage";
    }

    @PostMapping("/mypage/edit")
    public String updateUserInfo(@ModelAttribute SiteUser updatedUser,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal SiteUser currentUser) throws IOException {
        currentUser.setNickname(updatedUser.getNickname());

        String imageUrl = userService.uploadProfileImage(currentUser.getId(), file);
        currentUser.setProfileImageUrl(imageUrl);

        userService.updateUser(currentUser);
        return "redirect:/mypage";
    }

    @PostMapping("/mypage/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest request,
                                            @AuthenticationPrincipal SiteUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            switch (request.getField()) {
                case "nickname":
                    userService.updateNickname(user.getId(), request.getValue());
                    break;
                case "email":
                    userService.updateEmail(user.getId(), request.getValue());
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "잘못된 요청입니다."));
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "성공적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "변경 중 오류가 발생했습니다."));
        }
    }

    // 중복 체크 공통 메서드
    private String checkFieldDuplicate(String field, String value) {
        if ("nickname".equals(field)) {
            boolean nicknameExists = userService.isNicknameAlreadyRegistered(value);
            if (nicknameExists) {
                return "이미 등록된 닉네임입니다.";
            }
        } else if ("email".equals(field)) {
            boolean emailExists = userService.isEmailAlreadyRegistered(value);
            if (emailExists) {
                return "이미 등록된 이메일입니다.";
            }
        }
        return null; // 중복이 없으면 null 반환
    }

    @PostMapping("/uploadProfileImage")
    public String uploadProfileImage(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal SiteUser siteUser) throws IOException {
        String imageUrl = userService.uploadProfileImage(siteUser.getId(), file);
        siteUser.setProfileImageUrl(imageUrl);
        userService.updateUser(siteUser);
        return "redirect:/mypage";
    }

    @GetMapping("/mypage/change_password")
    public String changePasswordForm() {
        return "#";
    }

    @PostMapping("/mypage/change_password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @AuthenticationPrincipal SiteUser siteUser) {
        if(passwordEncoder.matches(currentPassword, siteUser.getPassword())) {
            siteUser.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(siteUser);
            return "redirect:/mypage";
        }else {
            return "redirect:/mypage/change_password?error";
        }
    }

    @PostMapping("/mypage/delete")
    public String deleteUser(@AuthenticationPrincipal SiteUser siteUser) {
        userService.deleteUser(siteUser.getId());
        return "redirect:/";
    }


    // 사용자 정보를 반환하는 메서드 (예: 닉네임, 프로필 이미지 URL 등)
    @GetMapping("/userInfo/{userId}")
    public ResponseEntity<SiteUser> getUserInfo(@PathVariable Long userId) {
        SiteUser user = userService.getUserById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
