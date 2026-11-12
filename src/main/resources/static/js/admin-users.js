
const USER_API_URL = "/api/admin/users";

const userTableBody = document.getElementById("userTableBody");
const searchUserInput = document.getElementById("searchUserInput");
const searchUserBtn = document.getElementById("searchUserBtn");
const resetUserBtn = document.getElementById("resetUserBtn");
const userMessage = document.getElementById("userMessage");

const userDetailModal = document.getElementById("userDetailModal");
const userDetailContent = document.getElementById("userDetailContent");

let allUsers = [];

// Lấy token nếu frontend đang lưu JWT trong localStorage.
// Nếu dùng cookie, trình duyệt sẽ tự gửi cookie cùng request.
function getAuthHeaders() {
    const token =
        localStorage.getItem("accessToken") ||
        localStorage.getItem("token");

    const headers = {};

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

// Gọi API
async function userApiRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: "same-origin",
        headers: {
            ...getAuthHeaders(),
            ...options.headers
        }
    });

    const rawBody = await response.text();

    let data = null;

    try {
        data = rawBody ? JSON.parse(rawBody) : null;
    } catch {
        data = rawBody;
    }

    if (!response.ok) {
        const message =
            data?.message ||
            (typeof data === "string" ? data : null) ||
            `Lỗi HTTP ${response.status}`;

        throw new Error(message);
    }

    return data;
}

// Hiển thị thông báo
function showUserMessage(message, isError = false) {
    userMessage.textContent = message;
    userMessage.className = isError
        ? "user-message error"
        : "user-message success";
}

// Định dạng ngày
function formatDate(date) {
    if (!date) return "Chưa có thông tin";

    return new Date(date).toLocaleString("vi-VN");
}

// Cập nhật thống kê
function updateUserStatistics(users) {
    document.getElementById("totalUsers").textContent =
        users.length;

    document.getElementById("activeUsers").textContent =
        users.filter(user => user.active).length;

    document.getElementById("blockedUsers").textContent =
        users.filter(user => !user.active).length;

    document.getElementById("adminUsers").textContent =
        users.filter(user =>
            user.role?.toUpperCase() === "ADMIN"
        ).length;
}

// Tạo ô bảng an toàn
function createCell(value) {
    const td = document.createElement("td");
    td.textContent = value ?? "—";
    return td;
}

// Hiển thị danh sách
function displayUsers(users) {
    userTableBody.replaceChildren();

    if (!users || users.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");

        td.colSpan = 7;
        td.textContent = "Không tìm thấy người dùng.";

        tr.appendChild(td);
        userTableBody.appendChild(tr);
        return;
    }

    users.forEach(user => {
        const tr = document.createElement("tr");

        tr.appendChild(createCell(user.userId));
        tr.appendChild(createCell(user.fullName));
        tr.appendChild(createCell(user.email));
        tr.appendChild(createCell(user.phone));

        const roleCell = createCell(user.role);
        roleCell.classList.add(
            user.role?.toUpperCase() === "ADMIN"
                ? "user-role-admin"
                : "user-role-normal"
        );
        tr.appendChild(roleCell);

        const statusCell = document.createElement("td");
        const statusBadge = document.createElement("span");

        statusBadge.className = user.active
            ? "user-status active"
            : "user-status blocked";

        statusBadge.textContent = user.active
            ? "Đang hoạt động"
            : "Đã khóa";

        statusCell.appendChild(statusBadge);
        tr.appendChild(statusCell);

        const actionCell = document.createElement("td");
        const actionContainer = document.createElement("div");

        actionContainer.className = "user-action-buttons";

        const detailBtn = document.createElement("button");
        detailBtn.textContent = "Chi tiết";
        detailBtn.className = "user-detail-btn";
        detailBtn.addEventListener("click", () => {
            showUserDetail(user.userId);
        });

        actionContainer.appendChild(detailBtn);

        if (user.role?.toUpperCase() !== "ADMIN") {
            const statusBtn = document.createElement("button");

            statusBtn.textContent = user.active
                ? "Khóa"
                : "Mở khóa";

            statusBtn.className = user.active
                ? "user-block-btn"
                : "user-unblock-btn";

            statusBtn.addEventListener("click", () => {
                changeUserStatus(user);
            });

            actionContainer.appendChild(statusBtn);
        }

        actionCell.appendChild(actionContainer);
        tr.appendChild(actionCell);

        userTableBody.appendChild(tr);
    });
}

// Tải danh sách người dùng
async function loadUsers() {
    try {
        showUserMessage("");

        const users = await userApiRequest(USER_API_URL);

        allUsers = users || [];

        updateUserStatistics(allUsers);
        displayUsers(allUsers);

    } catch (error) {
        showUserMessage(
            "Không thể tải danh sách: " + error.message,
            true
        );
    }
}

// Tìm kiếm
async function searchUsers() {
    const keyword = searchUserInput.value.trim();

    if (!keyword) {
        displayUsers(allUsers);
        return;
    }

    try {
        showUserMessage("");

        const users = await userApiRequest(
            `${USER_API_URL}/search?keyword=${encodeURIComponent(keyword)}`
        );

        displayUsers(users || []);

    } catch (error) {
        showUserMessage(
            "Tìm kiếm thất bại: " + error.message,
            true
        );
    }
}

// Xem chi tiết
async function showUserDetail(userId) {
    try {
        const user = await userApiRequest(
            `${USER_API_URL}/${userId}`
        );

        userDetailContent.replaceChildren();

        const fields = [
            ["Mã người dùng", user.userId],
            ["Họ và tên", user.fullName],
            ["Email", user.email],
            ["Số điện thoại", user.phone || "Chưa cập nhật"],
            ["Vai trò", user.role],
            ["Trạng thái", user.active ? "Đang hoạt động" : "Đã khóa"],
            ["Ngày tạo", formatDate(user.createdAt)]
        ];

        fields.forEach(([label, value]) => {
            const row = document.createElement("p");
            const strong = document.createElement("strong");

            strong.textContent = `${label}: `;
            row.append(strong, document.createTextNode(value ?? "—"));

            userDetailContent.appendChild(row);
        });

        userDetailModal.classList.remove("hidden");

    } catch (error) {
        showUserMessage(
            "Không thể xem chi tiết: " + error.message,
            true
        );
    }
}

// Khóa / mở khóa
async function changeUserStatus(user) {
    const newStatus = !user.active;

    const action = newStatus ? "mở khóa" : "khóa";

    const confirmed = confirm(
        `Bạn có chắc chắn muốn ${action} tài khoản ${user.email}?`
    );

    if (!confirmed) return;

    try {
        await userApiRequest(
            `${USER_API_URL}/${user.userId}/status`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    active: newStatus
                })
            }
        );

        showUserMessage(
            `Đã ${action} tài khoản thành công.`
        );

        await loadUsers();

    } catch (error) {
        showUserMessage(
            `Không thể ${action} tài khoản: ${error.message}`,
            true
        );
    }
}

// Đóng modal
function closeUserDetail() {
    userDetailModal.classList.add("hidden");
}

// Sự kiện
searchUserBtn.addEventListener("click", searchUsers);

searchUserInput.addEventListener("keydown", event => {
    if (event.key === "Enter") {
        searchUsers();
    }
});

resetUserBtn.addEventListener("click", () => {
    searchUserInput.value = "";
    showUserMessage("");
    displayUsers(allUsers);
});

document.getElementById("closeUserDetailBtn")
    .addEventListener("click", closeUserDetail);

document.getElementById("closeUserDetailBtn2")
    .addEventListener("click", closeUserDetail);

userDetailModal.addEventListener("click", event => {
    if (event.target === userDetailModal) {
        closeUserDetail();
    }
});

// Khởi tạo
loadUsers();
