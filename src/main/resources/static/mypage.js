document.addEventListener("DOMContentLoaded", function() {

    // 모달 열기
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