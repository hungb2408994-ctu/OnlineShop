document.addEventListener("DOMContentLoaded", function () {

    const accountLink = document.getElementById("accountLink");

    if (!accountLink) return;

    const token = localStorage.getItem("accessToken");
    const currentUserData = localStorage.getItem("currentUser");

    // Chưa đăng nhập
    if (!token || !currentUserData) {
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

    try {
        const user = JSON.parse(currentUserData);

        // Đã đăng nhập
        accountLink.href = "#";
        accountLink.innerHTML = `
            <span class="account-icon">♙</span>
            <span class="account-info">
                <strong id="headerUserName"></strong>
                <small id="logoutButton">Đăng xuất</small>
            </span>
        `;

        document.getElementById("headerUserName").textContent =
            user.fullName || user.email;

        // Xử lý đăng xuất
        document.getElementById("logoutButton")
            .addEventListener("click", function (event) {

                event.preventDefault();
                event.stopPropagation();

                localStorage.removeItem("accessToken");
                localStorage.removeItem("currentUser");

                window.location.href = "/";
            });

    } catch (error) {
        console.error("Không thể đọc thông tin người dùng:", error);

        localStorage.removeItem("currentUser");
        localStorage.removeItem("accessToken");

        window.location.href = "/login";
    }
});