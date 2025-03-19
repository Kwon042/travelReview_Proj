package com.example.user;

import com.example.board.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
        // profileImageUrl 값을 출력
        System.out.println("Profile Image URL: " + user.getProfileImageUrl());
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

    // 사용자 닉네임 및 이메일 업데이트 메서드
    @PostMapping("/mypage/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest request,
                                            @AuthenticationPrincipal SiteUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
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
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "잘못된 요청입니다."));
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "변경 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/uploadProfileImage")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal SiteUser user,
            @RequestParam("profileImage") MultipartFile file) {

        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 프로필 이미지 업로드 로직
            String imageUrl = userService.uploadProfileImage(user.getId(), file);
            System.out.println("Uploaded image URL: " + imageUrl); // 새로 업로드한 이미지 URL 출력

            return ResponseEntity.ok(Map.of("success", true, "newProfileImageUrl", imageUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            // 예외 발생 시 로그를 남깁니다.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "파일 업로드 중 오류가 발생했습니다."));
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "예기치 않은 오류가 발생했습니다."));
        }
    }

    @GetMapping("/mypage/change_password")
    public String showChangePasswordPage() {
        // 모달로 보내기
        return "redirect:/user/mypage";
    }

    @PostMapping("/mypage/change_password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @AuthenticationPrincipal SiteUser siteUser) {
        if(passwordEncoder.matches(currentPassword, siteUser.getPassword())) {
            siteUser.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(siteUser);
            return "redirect:/user/mypage";
        }else {
            return "redirect:/user/mypage/change_password?error";
        }
    }

    @PostMapping("/mypage/delete")
    public String deleteUser(@AuthenticationPrincipal SiteUser siteUser) {
        userService.deleteUser(siteUser.getId());
        return "redirect:/";
    }

    @GetMapping("/getCurrentUserId")
    public ResponseEntity<Map<String, Object>> getCurrentUserId(@AuthenticationPrincipal SiteUser user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of("success", true, "userId", user.getId()));
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal SiteUser user, HttpServletRequest request, HttpServletResponse response) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            userService.deleteUser(user.getId());

            // 로그아웃 처리
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication()); // Authentication 추가

            return ResponseEntity.ok(Map.of("success", true, "message", "계정이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "계정 삭제 중 오류가 발생했습니다."));
        }
    }


}
