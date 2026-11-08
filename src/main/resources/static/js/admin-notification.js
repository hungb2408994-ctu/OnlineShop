
document.addEventListener("DOMContentLoaded", () => {
    const bell = document.getElementById("notificationBell");
    const badge = document.getElementById("notificationBadge");
    const panel = document.getElementById("notificationPanel");
    const list = document.getElementById("notificationList");
    const markAllButton = document.getElementById("markAllReadBtn");

    if (!bell || !badge || !panel || !list || !markAllButton) {
        return;
    }

    const API = "/api/admin/notifications";

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            }
        });

        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }

        return response.json();
    }

    async function loadUnreadCount() {
        try {
            const data = await apiRequest(`${API}/unread-count`);
            const count = Number(data.unreadCount || 0);

            badge.textContent = count > 99 ? "99+" : count;
            badge.hidden = count === 0;
        } catch (error) {
            console.error("Cannot load unread notifications:", error);
        }
    }

    function formatDate(dateString) {
        return new Date(dateString).toLocaleString("vi-VN");
    }

    function renderNotifications(notifications) {
        list.replaceChildren();

        if (!notifications.length) {
            const empty = document.createElement("p");
            empty.className = "notification-empty";
            empty.textContent = "Bạn chưa có thông báo nào.";
            list.appendChild(empty);
            return;
        }

        notifications.forEach(notification => {
            const item = document.createElement("div");
            item.className = "notification-item";

            if (!notification.read) {
                item.classList.add("unread");
            }

            const title = document.createElement("strong");
            title.textContent = notification.title;

            const message = document.createElement("p");
            message.textContent = notification.message;

            const date = document.createElement("small");
            date.textContent = formatDate(notification.createdAt);

            item.append(title, message, date);

            if (!notification.read) {
                item.addEventListener("click", async () => {

                    try {
                        await apiRequest(
                            `/api/admin/notifications/${notification.id}/read`,
                            { method: "PUT" }
                        );

                        if (notification.orderId) {
                            window.location.href =
                                `/admin/orders/${notification.orderId}`;
                        } else {
                            await loadNotifications();
                        }

                    } catch (error) {
                        console.error("Không thể xử lý thông báo:", error);
                    }

                });
            }

            list.appendChild(item);
        });
    }

    async function loadNotifications() {
        try {
            const notifications = await apiRequest(API);
            renderNotifications(notifications);
        } catch (error) {
            console.error("Cannot load notifications:", error);
            list.textContent = "Không thể tải thông báo.";
        }
    }

    bell.addEventListener("click", async (event) => {
        event.stopPropagation();

        panel.hidden = !panel.hidden;

        if (!panel.hidden) {
            await loadNotifications();
        }
    });

    panel.addEventListener("click", event => {
        event.stopPropagation();
    });

    document.addEventListener("click", () => {
        panel.hidden = true;
    });

    markAllButton.addEventListener("click", async () => {
        try {
            await apiRequest(`${API}/read-all`, {
                method: "PUT"
            });

            await loadNotifications();
            await loadUnreadCount();
        } catch (error) {
            console.error("Cannot mark all notifications as read:", error);
            alert("Không thể cập nhật thông báo.");
        }
    });

    loadUnreadCount();

    // Cập nhật số thông báo chưa đọc mỗi 10 giây
    setInterval(loadUnreadCount, 10000);
});
