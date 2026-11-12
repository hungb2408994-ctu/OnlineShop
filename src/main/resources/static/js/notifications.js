
document.addEventListener("DOMContentLoaded", function () {
    if (!isLoggedIn()) {
        window.location.href =
            "/login?next=" + encodeURIComponent("/notifications");
        return;
    }

    loadNotifications();

    document.getElementById("markAllReadBtn")
        .addEventListener("click", markAllAsRead);
});

/* ==============================
   API
============================== */

const NOTIFICATION_API = "/api/notifications";

async function notificationRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Authorization": `Bearer ${getAccessToken()}`,
            ...(options.headers || {})
        }
    });

    if (response.status === 401) {
        clearSession();
        window.location.href =
            "/login?next=" + encodeURIComponent("/notifications");
        throw new Error("Phiên đăng nhập đã hết hạn.");
    }

    if (!response.ok) {
        let message = "Có lỗi xảy ra.";

        try {
            const data = await response.json();
            message = data.message || message;
        } catch (_) {
            // Response không có JSON body.
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

/* ==============================
   LOAD NOTIFICATIONS
============================== */

async function loadNotifications() {
    const list = document.getElementById("notificationList");

    list.innerHTML = `
        <div class="notification-loading">
            Đang tải thông báo...
        </div>
    `;

    try {
        const notifications = await notificationRequest(
            NOTIFICATION_API
        );

        renderNotifications(notifications || []);
        updateNotificationSummary(notifications || []);

        // Cập nhật số thông báo trên chuông header.
        if (typeof loadNotificationCount === "function") {
            loadNotificationCount();
        }

    } catch (error) {
        list.innerHTML = `
            <div class="notification-error">
                ${escapeNotificationHTML(error.message)}
            </div>
        `;
    }
}

/* ==============================
   RENDER
============================== */

function renderNotifications(notifications) {
    const list = document.getElementById("notificationList");

    document.getElementById("notificationCount").textContent =
        `${notifications.length} thông báo`;

    if (notifications.length === 0) {
        list.innerHTML = `
            <div class="notification-empty">
                Bạn chưa có thông báo nào.
            </div>
        `;
        return;
    }

    list.replaceChildren();

    notifications.forEach(notification => {
        const item = document.createElement("article");

        item.className = notification.read
            ? "notification-item"
            : "notification-item unread";

        const icon = document.createElement("div");
        icon.className = "notification-icon";
        icon.textContent = "♧";

        const content = document.createElement("div");
        content.className = "notification-content";

        const title = document.createElement("h3");
        title.textContent = notification.title || "Thông báo";

        const message = document.createElement("p");
        message.textContent = notification.message || "";

        const time = document.createElement("span");
        time.className = "notification-time";
        time.textContent = formatNotificationDate(
            notification.createdAt
        );

        content.append(title, message, time);

        const actions = document.createElement("div");
        actions.className = "notification-actions";

        if (notification.orderId != null) {
            const orderLink = document.createElement("a");
            orderLink.href = "/my-orders";
            orderLink.textContent = "Xem đơn hàng";

            orderLink.addEventListener("click", async function (event) {
                if (!notification.read) {
                    event.preventDefault();

                    try {
                        await markNotificationAsRead(
                            notification.id,
                            false
                        );

                        window.location.href = "/my-orders";
                    } catch (error) {
                        showNotificationMessage(
                            error.message,
                            "error"
                        );
                    }
                }
            });

            actions.appendChild(orderLink);
        }

        if (!notification.read) {
            const readButton = document.createElement("button");
            readButton.type = "button";
            readButton.textContent = "Đánh dấu đã đọc";

            readButton.addEventListener("click", async function () {
                try {
                    await markNotificationAsRead(
                        notification.id,
                        true
                    );
                } catch (error) {
                    showNotificationMessage(
                        error.message,
                        "error"
                    );
                }
            });

            actions.appendChild(readButton);
        }

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "delete-notification";
        deleteButton.textContent = "Xóa";

        deleteButton.addEventListener("click", async function () {
            try {
                await deleteNotification(notification.id);
            } catch (error) {
                showNotificationMessage(
                    error.message,
                    "error"
                );
            }
        });

        actions.appendChild(deleteButton);

        item.append(icon, content, actions);
        list.appendChild(item);
    });
}

/* ==============================
   SUMMARY
============================== */

function updateNotificationSummary(notifications) {
    const unreadCount = notifications.filter(
        notification => !notification.read
    ).length;

    document.getElementById("totalNotifications").textContent =
        notifications.length;

    document.getElementById("unreadNotifications").textContent =
        unreadCount;
}

/* ==============================
   MARK AS READ
============================== */

async function markNotificationAsRead(id, shouldReload = true) {
    await notificationRequest(
        `${NOTIFICATION_API}/${id}/read`,
        { method: "PUT" }
    );

    if (shouldReload) {
        showNotificationMessage(
            "Đã đánh dấu thông báo là đã đọc.",
            "success"
        );

        await loadNotifications();
    }
}

async function markAllAsRead() {
    const button = document.getElementById("markAllReadBtn");

    button.disabled = true;

    try {
        await notificationRequest(
            `${NOTIFICATION_API}/read-all`,
            { method: "PUT" }
        );

        showNotificationMessage(
            "Đã đánh dấu tất cả thông báo là đã đọc.",
            "success"
        );

        await loadNotifications();

    } catch (error) {
        showNotificationMessage(error.message, "error");
    } finally {
        button.disabled = false;
    }
}

/* ==============================
   DELETE
============================== */

async function deleteNotification(id) {
    await notificationRequest(
        `${NOTIFICATION_API}/${id}`,
        { method: "DELETE" }
    );

    showNotificationMessage(
        "Đã xóa thông báo.",
        "success"
    );

    await loadNotifications();
}

/* ==============================
   UTILITIES
============================== */

function formatNotificationDate(dateString) {
    if (!dateString) return "";

    const date = new Date(dateString);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    });
}

function escapeNotificationHTML(value) {
    const element = document.createElement("span");
    element.textContent = value;
    return element.innerHTML;
}

function showNotificationMessage(message, type) {
    const element = document.getElementById("notificationMessage");

    element.textContent = message;
    element.className = `notification-message ${type}`;
}
