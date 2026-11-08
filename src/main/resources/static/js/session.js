// ==========================================================
// QUẢN LÝ PHIÊN ĐĂNG NHẬP (dùng chung cho các trang)
// ==========================================================

const ACCESS_TOKEN_KEY = "accessToken";
const CURRENT_USER_KEY = "currentUser";

// Lấy JWT đang lưu trong trình duyệt
function getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
}

// Lấy thông tin người dùng đang đăng nhập
function getCurrentUser() {
    const raw = localStorage.getItem(CURRENT_USER_KEY);

    if (!raw) return null;

    try {
        return JSON.parse(raw);
    } catch (error) {
        clearSession();
        return null;
    }
}

// Đã đăng nhập khi có cả token và thông tin người dùng
function isLoggedIn() {
    return Boolean(getAccessToken() && getCurrentUser());
}

// Lưu JWT và thông tin người dùng sau khi đăng nhập thành công
function saveSession(data) {
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);

    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify({
        userId: data.userId,
        fullName: data.fullName,
        email: data.email,
        role: data.role
    }));
}

// Xóa phiên đăng nhập hiện tại
function clearSession() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(CURRENT_USER_KEY);
}

// Đăng xuất và quay về trang chủ
function logout() {
    clearSession();
    window.location.href = "/";
}

// Đọc tham số ?next= nhưng chỉ chấp nhận đường dẫn nội bộ
// (tránh bị lợi dụng để chuyển sang tên miền khác)
function getSafeNextPath() {
    const next = new URLSearchParams(window.location.search).get("next");

    if (!next) return null;

    // Bắt buộc là đường dẫn tuyệt đối trong cùng website
    if (!next.startsWith("/")) return null;
    if (next.startsWith("//") || next.startsWith("/\\")) return null;

    // Không quay lại chính trang đăng nhập/đăng ký để tránh vòng lặp
    if (next === "/login" || next.startsWith("/login?") ||
        next === "/register" || next.startsWith("/register?")) {
        return null;
    }

    return next;
}

// Tạo URL trang đăng nhập kèm trang hiện tại để quay lại sau khi đăng nhập
function buildLoginUrl(sessionExpired) {
    const currentPath = window.location.pathname + window.location.search;

    let url = "/login?next=" + encodeURIComponent(currentPath);

    if (sessionExpired) {
        url += "&expired=1";
    }

    return url;
}

// Chuyển người dùng sang trang đăng nhập
function redirectToLogin(sessionExpired) {
    window.location.href = buildLoginUrl(sessionExpired);
}
