document.addEventListener("DOMContentLoaded", function () {
    // 🔹 프로필 이미지 모달 열기
    function openProfileImageModal() {
        const modal = document.getElementById('profileImageModal');
        if (modal) {
            modal.classList.remove('hidden');  // 'hidden' 클래스를 제거하여 모달 열기
        } else {
            console.error("모달 요소를 찾을 수 없습니다.");
        }
    }

    // 🔹 프로필 이미지 모달 닫기
    function closeProfileImageModal() {
        const modal = document.getElementById('profileImageModal');
        if (modal) {
            modal.classList.add('hidden');  // 'hidden' 클래스를 추가하여 모달 닫기
        }
    }

    // 🔹 비밀번호 수정 폼 보이기/숨기기
    function togglePasswordForm() {
        const form = document.getElementById('changePasswordForm');
        form.style.display = (form.style.display === "none") ? "block" : "none";
    }

    // 🔹 사용자 정보 수정 모달 열기
    function openEditModal(field) {
        let modalContent = `
            <span class="close" onclick="closeEditModal()">&times;</span> <!-- X 닫기 버튼 추가 -->
        `;

        switch (field) {
            case 'nickname':
                modalContent += `
                    <h4>닉네임 수정</h4>
                    <input type="text" id="editNickname" placeholder="새 닉네임" required>
                    <button id="saveNickname">저장</button>
                `;
                break;
            case 'email':
                modalContent += `
                    <h4>이메일 수정</h4>
                    <input type="email" id="editEmail" placeholder="새 이메일" required>
                    <button id="saveEmail">저장</button>
                `;
                break;
            default:
                return;
        }

        const modal = document.getElementById('editModal');
        const content = modal.querySelector('.modal-content');
        content.innerHTML = modalContent;
        modal.style.display = 'flex'; // 모달 열기

        // 저장 버튼 클릭 이벤트 추가 (한 번만 추가되도록 조치)
        setTimeout(() => {
            document.getElementById(`save${field.charAt(0).toUpperCase() + field.slice(1)}`)
                .addEventListener("click", () => saveEdit(field));
        }, 10);
    }

    // 🔹 정보 저장
    function saveEdit(field) {
        let inputValue;
        let updateField;

        if (field === 'nickname') {
            inputValue = document.getElementById('editNickname').value;
            updateField = 'nickname';
        } else if (field === 'email') {
            inputValue = document.getElementById('editEmail').value;
            updateField = 'email';
        } else {
            return;
        }

        // 🔹 서버로 닉네임(또는 이메일) 변경 요청 보내기
        fetch(`/user/mypage/update`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                field: updateField,
                value: inputValue
            })
        })
        .then(response => {
            if (!response.ok) {
                // 상태 코드가 200 ~ 299 범위가 아니면 오류 처리
                return response.json().then(data => {
                    throw new Error(data.message || `서버에서 오류가 발생했습니다. 상태 코드: ${response.status}`);
                });
            }
            return response.json(); // JSON 응답 처리
        })
        .then(data => {
            if (data.success) {
                // 서버에서 받은 메시지 출력
                alert(data.message);

                // 화면에 즉시 반영
                if (field === 'nickname') {
                    const currentNickname = document.getElementById('currentNickname');
                    if (currentNickname) {
                        currentNickname.innerText = inputValue;
                    }
                } else if (field === 'email') {
                    const currentEmail = document.getElementById('currentEmail');
                    if (currentEmail) {
                        currentEmail.innerText = inputValue;
                    }
                }
                closeEditModal(); // 모달 닫기
            } else {
               // 서버에서 받은 중복 메시지 처리
               if (data.message === "이미 등록된 닉네임입니다.") {
                   alert("이미 등록된 닉네임입니다.");
               } else if (data.message === "이미 등록된 이메일입니다.") {
                   alert("이미 등록된 이메일입니다.");
               } else {
                   alert(`변경에 실패했습니다: ${data.message}`);
               }
            }
        })
        .catch(error => {
            console.error("업데이트 중 오류 발생:", error);
            alert("변경 중 오류가 발생했습니다.");
        });
    }

    // 🔹 모달 닫기
    function closeEditModal() {
        const modal = document.getElementById('editModal');
        if (modal) {
            modal.style.display = 'none'; // 모달 숨기기
        }
    }

    // 🔹 회원 탈퇴 버튼 이벤트 등록
    const deleteAccountButton = document.getElementById("deleteAccountButton");
    if (deleteAccountButton) {
        deleteAccountButton.addEventListener("click", function () {
            if (confirm("정말로 회원 탈퇴를 하시겠습니까?")) {
                fetch("/user/deleteAccount", {
                    method: "DELETE"
                }).then(response => {
                    if (response.ok) {
                        alert("회원 탈퇴가 완료되었습니다.");
                        window.location.href = "/";
                    } else {
                        alert("탈퇴에 실패했습니다.");
                    }
                });
            }
        });
    }

    // 🔹 전역에서 접근할 수 있도록 함수 등록
    window.openProfileImageModal = openProfileImageModal;
    window.closeProfileImageModal = closeProfileImageModal;
    window.openEditModal = openEditModal;
    window.togglePasswordForm = togglePasswordForm;
    window.closeEditModal = closeEditModal;
});
