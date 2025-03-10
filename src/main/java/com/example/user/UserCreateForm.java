package com.example.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {
    @Size(min = 3, max = 25)
    @NotEmpty(message = "사용자ID는 필수항목입니다.")
    private String username;
    
    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    private String password1;
    
    @NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
    private String password2;

    @NotEmpty(message = "이메일은 필수항목입니다.")
    private String email;
    
    @NotEmpty(message = "닉네임을 적어주세요")
    private String nickname;

    // 비밀번호와 비밀번호 확인이 일치하는지 검증하는 메서드
    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordEqual() {
        return password1 != null && password1.equals(password2);
    }
}
