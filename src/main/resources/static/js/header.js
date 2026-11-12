document.addEventListener("DOMContentLoaded", function () {
  const user = isLoggedIn() ? getCurrentUser() : null;

  renderHomeAccount(user);
  renderProductsAccount(user);
  createProfileModal();
  bindAccountMenu();
});

/* ==============================
   RENDER ACCOUNT
============================== */

function createAccountMenu(user, wrapperId) {
  const wrapper = document.createElement("div");
  wrapper.className = "account-menu-wrapper";
  wrapper.id = wrapperId;

  const trigger = document.createElement("button");
  trigger.type = "button";
  trigger.className = "account-menu-trigger";
  trigger.setAttribute("aria-expanded", "false");

  const icon = document.createElement("span");
  icon.className = "account-icon";
  icon.textContent = "♙";

  const name = document.createElement("span");
  name.className = "account-name";
  name.textContent = user.fullName || user.email;

  trigger.append(icon, name);

  const dropdown = document.createElement("div");
  dropdown.className = "account-dropdown";
  dropdown.hidden = true;

  const profileButton = document.createElement("button");
  profileButton.type = "button";
  profileButton.textContent = "Thông tin cá nhân";
  profileButton.addEventListener("click", function () {
    dropdown.hidden = true;
    trigger.setAttribute("aria-expanded", "false");
    openProfileModal();
  });

  dropdown.appendChild(profileButton);
  // Liên kết đến trang đơn hàng của khách hàng
  const ordersLink = document.createElement("a");
  ordersLink.href = "/my-orders";
  ordersLink.textContent = "Đơn hàng của tôi";

  dropdown.appendChild(ordersLink);

  if (user.role === "ADMIN") {
    const adminLink = document.createElement("a");
    adminLink.href = "/admin/products";
    adminLink.textContent = "Trang quản trị";
    dropdown.appendChild(adminLink);
  }

  const logoutButton = document.createElement("button");
  logoutButton.type = "button";
  logoutButton.className = "account-logout";
  logoutButton.textContent = "Đăng xuất";
  logoutButton.addEventListener("click", logout);

  dropdown.appendChild(logoutButton);
  wrapper.append(trigger, dropdown);

  return wrapper;
}

function renderHomeAccount(user) {
  const accountLink = document.getElementById("accountLink");
  if (!accountLink) return;

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

  const wrapper = createAccountMenu(user, "homeAccountWrapper");
  accountLink.replaceWith(wrapper);
}

function renderProductsAccount(user) {
  const container = document.getElementById("headerAccount");
  if (!container) return;

  if (!user) {
    container.innerHTML = `
        <a href="/login" class="account-link">
            <span class="account-info">
                Đăng nhập
                <small>Đăng ký</small>
            </span>
        </a>
    `;
    return;
  }

  const wrapper = createAccountMenu(user, "productsAccountWrapper");

  container.replaceChildren(wrapper);
}

/* ==============================
   ACCOUNT DROPDOWN
============================== */

function bindAccountMenu() {
  document.addEventListener("click", function (event) {
    document.querySelectorAll(".account-menu-wrapper").forEach((wrapper) => {
      const trigger = wrapper.querySelector(".account-menu-trigger");
      const dropdown = wrapper.querySelector(".account-dropdown");

      if (!trigger || !dropdown) return;

      if (trigger.contains(event.target)) {
        const willOpen = dropdown.hidden;
        closeAllAccountMenus();

        dropdown.hidden = !willOpen;
        trigger.setAttribute("aria-expanded", String(willOpen));
        return;
      }

      if (!wrapper.contains(event.target)) {
        dropdown.hidden = true;
        trigger.setAttribute("aria-expanded", "false");
      }
    });
  });
}

function closeAllAccountMenus() {
  document.querySelectorAll(".account-menu-wrapper").forEach((wrapper) => {
    const trigger = wrapper.querySelector(".account-menu-trigger");
    const dropdown = wrapper.querySelector(".account-dropdown");

    if (trigger && dropdown) {
      dropdown.hidden = true;
      trigger.setAttribute("aria-expanded", "false");
    }
  });
}

/* ==============================
   PROFILE MODAL
============================== */

function createProfileModal() {
  if (document.getElementById("profileModal")) return;

  const modal = document.createElement("div");
  modal.id = "profileModal";
  modal.className = "profile-modal";
  modal.hidden = true;

  modal.innerHTML = `
        <div class="profile-modal-content" role="dialog"
             aria-modal="true" aria-labelledby="profileTitle">

            <div class="profile-modal-header">
                <h2 id="profileTitle">Thông tin cá nhân</h2>
                <button type="button" class="profile-close-btn"
                        id="profileCloseBtn">&times;</button>
            </div>

            <form id="profileForm">
                <div class="profile-form-group">
                    <label for="profileFullName">Họ và tên</label>
                    <input type="text" id="profileFullName"
                           maxlength="100" required>
                </div>

                <div class="profile-form-group">
                    <label for="profileEmail">Email</label>
                    <input type="email" id="profileEmail" disabled>
                </div>

                <div class="profile-form-group">
                    <label for="profilePhone">Số điện thoại</label>
                    <input type="tel" id="profilePhone"
                           maxlength="20">
                </div>

                <div class="profile-form-group">
                    <label for="profileRole">Vai trò</label>
                    <input type="text" id="profileRole" disabled>
                </div>

                <p id="profileMessage" class="profile-message"></p>

                <div class="profile-modal-actions">
                    <button type="button" class="profile-cancel-btn"
                            id="profileCancelBtn">Đóng</button>

                    <button type="submit" class="profile-save-btn"
                            id="profileSaveBtn">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    `;

  document.body.appendChild(modal);

  document
    .getElementById("profileCloseBtn")
    .addEventListener("click", closeProfileModal);

  document
    .getElementById("profileCancelBtn")
    .addEventListener("click", closeProfileModal);

  modal.addEventListener("click", function (event) {
    if (event.target === modal) {
      closeProfileModal();
    }
  });

  document
    .getElementById("profileForm")
    .addEventListener("submit", saveProfile);

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      closeProfileModal();
    }
  });
}

async function openProfileModal() {
  const modal = document.getElementById("profileModal");
  const message = document.getElementById("profileMessage");

  modal.hidden = false;
  message.textContent = "Đang tải thông tin...";
  message.className = "profile-message";

  document.getElementById("profileSaveBtn").disabled = true;

  try {
    const response = await fetch("/api/users/me", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${getAccessToken()}`,
      },
    });

    if (response.status === 401) {
      clearSession();
      window.location.href = "/login";
      return;
    }

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Không thể tải thông tin cá nhân.");
    }

    document.getElementById("profileFullName").value = data.fullName || "";

    document.getElementById("profileEmail").value = data.email || "";

    document.getElementById("profilePhone").value = data.phone || "";

    document.getElementById("profileRole").value = data.role || "USER";

    message.textContent = "";
    document.getElementById("profileSaveBtn").disabled = false;
  } catch (error) {
    message.textContent = error.message;
    message.className = "profile-message error";
  }
}

async function saveProfile(event) {
  event.preventDefault();

  const fullName = document.getElementById("profileFullName").value.trim();

  const phone = document.getElementById("profilePhone").value.trim();

  const message = document.getElementById("profileMessage");
  const saveButton = document.getElementById("profileSaveBtn");

  if (!fullName) {
    message.textContent = "Họ và tên không được để trống.";
    message.className = "profile-message error";
    return;
  }

  saveButton.disabled = true;
  message.textContent = "Đang lưu...";
  message.className = "profile-message";

  try {
    const response = await fetch("/api/users/me", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${getAccessToken()}`,
      },
      body: JSON.stringify({
        fullName: fullName,
        phone: phone,
      }),
    });

    if (response.status === 401) {
      clearSession();
      window.location.href = "/login";
      return;
    }

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Cập nhật thông tin thất bại.");
    }

    // Cập nhật tên trong localStorage, giữ nguyên email và role.
    const currentUser = getCurrentUser();

    if (currentUser) {
      currentUser.fullName = data.fullName;
      localStorage.setItem("currentUser", JSON.stringify(currentUser));
    }

    // Cập nhật tên đang hiển thị trên header.
    document.querySelectorAll(".account-name").forEach((element) => {
      element.textContent = data.fullName || data.email;
    });

    document.getElementById("profileFullName").value = data.fullName || "";

    document.getElementById("profilePhone").value = data.phone || "";

    message.textContent = "Cập nhật thông tin thành công.";
    message.className = "profile-message success";
    // Tự động đóng popup sau 1,5 giây
    setTimeout(() => {
      closeProfileModal();
    }, 1000);
  } catch (error) {
    message.textContent = error.message;
    message.className = "profile-message error";
  } finally {
    saveButton.disabled = false;
  }
}

function closeProfileModal() {
  const modal = document.getElementById("profileModal");

  if (modal) {
    modal.hidden = true;
  }
  /* ==============================
   NOTIFICATION BADGE
============================== */

  async function loadNotificationCount() {
    const notificationLinks = document.querySelectorAll(
      ".notification-link, .header-notification",
    );

    if (notificationLinks.length === 0 || !isLoggedIn()) {
      return;
    }

    try {
      const response = await fetch("/api/notifications/unread-count", {
        headers: {
          Authorization: `Bearer ${getAccessToken()}`,
        },
      });

      if (!response.ok) {
        return;
      }

      const count = await response.json();

      notificationLinks.forEach((link) => {
        let badge = link.querySelector(".notification-badge");

        if (!badge) {
          badge = document.createElement("span");
          badge.className = "notification-badge";
          link.appendChild(badge);
        }

        badge.textContent = count > 99 ? "99+" : count;
        badge.hidden = count <= 0;
      });
    } catch (error) {
      console.error("Không thể tải số thông báo:", error);
    }
  }
}
