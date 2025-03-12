package com.example.user;

import com.example.board.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        return "#";
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

    @PostMapping("/updateNickname")
    public String updateNickname(@RequestParam Long userId, @RequestParam String nickname) {
        userService.updateNickname(userId, nickname);
        return "redirect:/mypage";
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
