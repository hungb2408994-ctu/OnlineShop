// ==========================================================
// ĐĂNG NHẬP / ĐĂNG KÝ
// ==========================================================

const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

// Hiển thị thông báo trên form
function showAuthMessage(message, type) {
    const messageElement = document.getElementById("authMessage");

    if (!messageElement) return;

    messageElement.textContent = message;
    messageElement.className = "auth-message " + type;
}

// Bật/tắt trạng thái đang gửi của nút bấm
function setButtonLoading(button, isLoading, loadingText, normalText) {
    button.disabled = isLoading;
    button.textContent = isLoading ? loadingText : normalText;
}

// Đọc nội dung JSON an toàn (server có thể trả về trang lỗi HTML)
async function readResponseBody(response) {
    try {
        return await response.json();
    } catch (error) {
        return {};
    }
}

// Chuyển thông báo lỗi kỹ thuật thành câu dễ hiểu
function getErrorMessage(error) {
    if (error instanceof TypeError) {
        return "Không thể kết nối đến máy chủ. Vui lòng thử lại sau.";
    }

    return error.message || "Đã xảy ra lỗi. Vui lòng thử lại.";
}

// Sau khi đăng nhập: quay lại trang trước đó nếu có.
// Không có trang chờ sẵn thì ADMIN vào thẳng trang quản trị, người dùng về trang chủ.
function redirectAfterLogin() {
    const next = getSafeNextPath();

    if (next) {
        window.location.replace(next);
        return;
    }

    const user = getCurrentUser();

    const defaultPath =
        user && user.role === "ADMIN" ? "/admin/products" : "/";

    window.location.replace(defaultPath);
}

// Giữ lại ?next= khi người dùng chuyển giữa trang đăng nhập và đăng ký
function keepNextPath(link) {
    if (!link) return;

    const next = getSafeNextPath();

    if (!next) return;

    link.href = link.getAttribute("href") + "?next=" + encodeURIComponent(next);
}

// Hiển thị thông báo theo trạng thái được chuyển sang từ trang khác
function showPageMessage() {
    const params = new URLSearchParams(window.location.search);

    if (params.get("expired") === "1") {
        showAuthMessage(
            "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
            "error"
        );
    } else if (params.get("registered") === "1") {
        showAuthMessage(
            "Đăng ký thành công! Vui lòng đăng nhập bằng tài khoản vừa tạo.",
            "success"
        );
    }
}

// Người dùng đã đăng nhập thì không cần ở lại trang đăng nhập/đăng ký
if ((loginForm || registerForm) && isLoggedIn()) {
    redirectAfterLogin();
} else {
    showPageMessage();
}

// Xử lý đăng nhập
if (loginForm) {

    keepNextPath(document.getElementById("registerLink"));

    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        if (!email || !password) {
            showAuthMessage("Vui lòng nhập email và mật khẩu.", "error");
            return;
        }

        const button = loginForm.querySelector("button[type='submit']");

        setButtonLoading(button, true, "Đang đăng nhập...", "Đăng nhập");

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

            const data = await readResponseBody(response);

            if (!response.ok) {
                throw new Error(data.message || "Đăng nhập thất bại!");
            }

            // Lưu JWT và thông tin người dùng vào phiên hiện tại
            saveSession(data);

            showAuthMessage(
                "Đăng nhập thành công! Đang chuyển trang...",
                "success"
            );

            redirectAfterLogin();

        } catch (error) {
            showAuthMessage(getErrorMessage(error), "error");
        } finally {
            setButtonLoading(button, false, "Đang đăng nhập...", "Đăng nhập");
        }
    });
}

// Xử lý đăng ký
if (registerForm) {

    keepNextPath(document.getElementById("loginLink"));

    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const fullName = document.getElementById("fullName").value.trim();
        const email = document.getElementById("email").value.trim();
        const phone = document.getElementById("phone").value.trim();
        const password = document.getElementById("password").value;

        if (!fullName || !email || !password) {
            showAuthMessage("Vui lòng nhập họ tên, email và mật khẩu.", "error");
            return;
        }

        const button = registerForm.querySelector("button[type='submit']");

        setButtonLoading(button, true, "Đang đăng ký...", "Đăng ký");

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

            const data = await readResponseBody(response);

            if (!response.ok) {
                throw new Error(data.message || "Đăng ký thất bại!");
            }

            showAuthMessage(
                "Đăng ký thành công! Đang chuyển đến trang đăng nhập...",
                "success"
            );

            registerForm.reset();

            // Quay lại trang đăng nhập kèm thông báo và trang cần quay lại
            const next = getSafeNextPath();

            let loginUrl = "/login?registered=1";

            if (next) {
                loginUrl += "&next=" + encodeURIComponent(next);
            }

            setTimeout(function () {
                window.location.replace(loginUrl);
            }, 1200);

        } catch (error) {
            showAuthMessage(getErrorMessage(error), "error");
        } finally {
            setButtonLoading(button, false, "Đang đăng ký...", "Đăng ký");
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
