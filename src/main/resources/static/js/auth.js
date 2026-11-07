// Hiển thị thông báo
function showAuthMessage(message, type) {
    const messageElement = document.getElementById("authMessage");

    if (!messageElement) return;

    messageElement.textContent = message;
    messageElement.className = "auth-message " + type;
}

// Xử lý đăng nhập
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        const button = loginForm.querySelector("button");
        button.disabled = true;
        button.textContent = "Đang đăng nhập...";

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Đăng nhập thất bại!");
            }

            // Lưu JWT và thông tin người dùng
            localStorage.setItem("accessToken", data.accessToken);

            localStorage.setItem("currentUser", JSON.stringify({
                userId: data.userId,
                fullName: data.fullName,
                email: data.email,
                role: data.role
            }));

            showAuthMessage("Đăng nhập thành công!", "success");

            // Chuyển về trang chủ
            window.location.href = "/";

        } catch (error) {
            showAuthMessage(error.message, "error");
        } finally {
            button.disabled = false;
            button.textContent = "Đăng nhập";
        }
    });
}

// Xử lý đăng ký
const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const fullName = document.getElementById("fullName").value.trim();
        const email = document.getElementById("email").value.trim();
        const phone = document.getElementById("phone").value.trim();
        const password = document.getElementById("password").value;

        const button = registerForm.querySelector("button");
        button.disabled = true;
        button.textContent = "Đang đăng ký...";

        try {
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    fullName: fullName,
                    email: email,
                    phone: phone,
                    password: password
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Đăng ký thất bại!");
            }

            showAuthMessage(
                "Đăng ký thành công! Đang chuyển đến trang đăng nhập...",
                "success"
            );

            registerForm.reset();

            setTimeout(function () {
                window.location.href = "/login";
            }, 1500);

        } catch (error) {
            showAuthMessage(error.message, "error");
        } finally {
            button.disabled = false;
            button.textContent = "Đăng ký";
        }
    });
}

// Bật/tắt hiển thị mật khẩu
document.querySelectorAll(".toggle-password").forEach(function (button) {

    button.addEventListener("click", function () {

        const targetId = button.dataset.target;
        const passwordInput = document.getElementById(targetId);

        if (!passwordInput) return;

        const isPassword = passwordInput.type === "password";

        // Chuyển đổi kiểu input
        passwordInput.type = isPassword ? "text" : "password";

        // Cập nhật trạng thái nút
        button.setAttribute("aria-pressed", String(isPassword));

        button.setAttribute(
            "aria-label",
            isPassword ? "Ẩn mật khẩu" : "Hiển thị mật khẩu"
        );

        // Đổi biểu tượng
        if (isPassword) {
            button.innerHTML = `
                <svg class="eye-icon" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor"
                     stroke-width="1.8"
                     stroke-linecap="round"
                     stroke-linejoin="round">
                    <path d="M3 3l18 18"/>
                    <path d="M10.6 5.1A10.9 10.9 0 0 1 12 5
                        c6.4 0 10 7 10 7a15.5 15.5 0 0 1-3 3.8"/>
                    <path d="M6.6 6.6C3.7 8.4 2 12 2 12
                        s3.6 7 10 7a10.5 10.5 0 0 0 4.2-.9"/>
                </svg>
            `;
        } else {
            button.innerHTML = `
                <svg class="eye-icon" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor"
                     stroke-width="1.8"
                     stroke-linecap="round"
                     stroke-linejoin="round">
                    <path d="M2 12s3.6-7 10-7 10 7 10 7
                        -3.6 7-10 7S2 12 2 12z"/>
                    <circle cx="12" cy="12" r="3"/>
                </svg>
            `;
        }
    });
});