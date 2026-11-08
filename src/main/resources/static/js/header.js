// ==========================================================
// HIỂN THỊ TRẠNG THÁI ĐĂNG NHẬP TRÊN HEADER
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {

    const user = isLoggedIn() ? getCurrentUser() : null;

    renderHomeAccount(user);
    renderProductsAccount(user);
});

// Header trang chủ: khối .account-link
function renderHomeAccount(user) {

    const accountLink = document.getElementById("accountLink");

    if (!accountLink) return;

    // Chưa đăng nhập
    if (!user) {
        accountLink.href = "/login";
        accountLink.innerHTML = `
            <span class="account-icon">♙</span>
            <span class="account-info">
                Đăng nhập
                <small>Đăng ký</small>
            </span>
        `;
        return;
    }

    // Đã đăng nhập: hiển thị tên và nút đăng xuất
    accountLink.href = "#";
    accountLink.innerHTML = `
        <span class="account-icon">♙</span>
        <span class="account-info">
            <strong class="account-name"></strong>
            <button type="button" class="logout-link">Đăng xuất</button>
        </span>
    `;

    accountLink.querySelector(".account-name").textContent =
        user.fullName || user.email;

    bindLogout(accountLink.querySelector(".logout-link"));
}

// Header trang sản phẩm: khối .header-account
function renderProductsAccount(user) {

    const container = document.getElementById("headerAccount");

    if (!container) return;

    const info = container.querySelector("div");

    if (!info) return;

    // Chưa đăng nhập
    if (!user) {
        info.innerHTML = `
            <a href="/login">Đăng nhập</a>
            <a href="/register">Đăng ký</a>
        `;
        return;
    }

    // Đã đăng nhập
    info.innerHTML = `
        <strong class="account-name"></strong>
        <button type="button" class="logout-link">Đăng xuất</button>
    `;

    info.querySelector(".account-name").textContent =
        user.fullName || user.email;

    bindLogout(info.querySelector(".logout-link"));
}

// Gắn sự kiện đăng xuất cho nút
function bindLogout(button) {

    if (!button) return;

    button.addEventListener("click", function (event) {
        event.preventDefault();
        event.stopPropagation();

        logout();
    });
}
