document.addEventListener("DOMContentLoaded", function() {

    // 프로필 이미지 모달 열기
    function openProfileImageModal() {
        console.log("프로필 이미지 모달을 열려고 시도했습니다.");
        document.getElementById('profileImageModal').style.display = 'flex';
    }

    // 모달 닫기
    function closeProfileImageModal() {
        document.getElementById('profileImageModal').style.display = 'none';
    }

    // 페이지 외부 클릭 시 모달 닫기
    window.onclick = function(event) {
        var modal = document.getElementById('profileImageModal');
        if (event.target == modal) {
            closeProfileImageModal();
        }
    }

    // 비밀번호 수정 폼 보이기/숨기기 함수
    function togglePasswordForm() {
        var form = document.getElementById('changePasswordForm');
        if (form.style.display === "none") {
            form.style.display = "block";
        } else {
            form.style.display = "none";
        }
    }

    // 사용자 정보 수정 모달 열기
    window.openEditModal = function(field) {
        // 각 필드에 따라 모달 내용 설정
        var modalContent = '';
        switch (field) {
            case 'nickname':
                modalContent = `
                    <h4>닉네임 수정</h4>
                    <input type="text" id="editNickname" placeholder="새 닉네임" required>
                    <button onclick="saveEdit('nickname')">저장</button>
                `;
                break;
            case 'email':
                modalContent = `
                    <h4>이메일 수정</h4>
                    <input type="email" id="editEmail" placeholder="새 이메일" required>
                    <button onclick="saveEdit('email')">저장</button>
                `;
                break;
            default:
                return;
        }

        // 모달 내용 업데이트
        var modal = document.getElementById('editModal');
        modal.querySelector('.modal-content').innerHTML = modalContent;
        modal.style.display = 'flex'; // 모달 열기
    }

    // 정보 저장
    window.saveEdit = function(field) {
        var inputValue;
        switch (field) {
            case 'nickname':
                inputValue = document.getElementById('editNickname').value;
                break;
            case 'email':
                inputValue = document.getElementById('editEmail').value;
                break;
            default:
                return;
        }
        // AJAX 요청 등으로 서버에 저장할 수 있음
        console.log(`Saving ${field}: ${inputValue}`);
        closeEditModal(); // 모달 닫기
    }

    // 모달 닫기
    function closeEditModal() {
        document.getElementById('editModal').style.display = 'none';
    }

    // 회원 탈퇴
    document.getElementById("deleteAccountButton").addEventListener("click", function() {
        if (confirm("정말로 회원 탈퇴를 하시겠습니까?")) {
            // 회원 탈퇴 요청 보내기
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
});