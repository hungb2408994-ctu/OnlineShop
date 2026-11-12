
document.addEventListener("DOMContentLoaded", function () {
    const user = isLoggedIn() ? getCurrentUser() : null;

    if (!user || !user.userId) {
        window.location.href = "/login?next=%2Fmy-orders";
        return;
    }

    initializeOrdersPage(user);
});

/* ==============================
   STATE
============================== */

let allOrders = [];
let filteredOrders = [];
let currentPage = 1;

const ORDERS_PER_PAGE = 5;

/* ==============================
   INITIALIZE
============================== */

function initializeOrdersPage(user) {
    document.getElementById("statusFilter")
        .addEventListener("change", function () {
            currentPage = 1;
            filterOrders();
        });

    document.getElementById("previousPage")
        .addEventListener("click", function () {
            if (currentPage > 1) {
                currentPage--;
                renderOrders();
            }
        });

    document.getElementById("nextPage")
        .addEventListener("click", function () {
            const totalPages = Math.ceil(
                filteredOrders.length / ORDERS_PER_PAGE
            );

            if (currentPage < totalPages) {
                currentPage++;
                renderOrders();
            }
        });

    document.getElementById("closeOrderModal")
        .addEventListener("click", closeOrderModal);

    document.getElementById("orderModal")
        .addEventListener("click", function (event) {
            if (event.target === this) {
                closeOrderModal();
            }
        });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeOrderModal();
        }
    });

    loadOrders(user.userId);
}

/* ==============================
   API
============================== */

async function loadOrders(userId) {
    const message = document.getElementById("ordersMessage");
    const tableWrapper = document.getElementById("ordersTableWrapper");

    message.hidden = false;
    message.textContent = "Đang tải đơn hàng...";
    message.className = "orders-message";

    tableWrapper.hidden = true;

    try {
        const response = await fetch(`/api/orders/${encodeURIComponent(userId)}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${getAccessToken()}`,
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            clearSession();
            window.location.href = "/login?next=%2Fmy-orders";
            return;
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                data?.message || "Không thể tải lịch sử đơn hàng."
            );
        }

        if (!Array.isArray(data)) {
            throw new Error("Dữ liệu đơn hàng không hợp lệ.");
        }

        allOrders = data.sort((a, b) => {
            return new Date(b.orderDate) - new Date(a.orderDate);
        });

        updateOrderSummary();
        filterOrders();

    } catch (error) {
        message.hidden = false;
        message.className = "orders-message error";
        message.textContent = error.message;
    }
}

/* ==============================
   SUMMARY
============================== */

function normalizeStatus(status) {
    return String(status || "").trim().toUpperCase();
}

function updateOrderSummary() {
    document.getElementById("totalOrders").textContent =
        allOrders.length;

    document.getElementById("pendingOrders").textContent =
        allOrders.filter(order =>
            normalizeStatus(order.status) === "PENDING"
        ).length;

    document.getElementById("shippingOrders").textContent =
        allOrders.filter(order =>
            ["SHIPPED", "PROCESSING"].includes(
                normalizeStatus(order.status)
            )
        ).length;

    document.getElementById("deliveredOrders").textContent =
        allOrders.filter(order =>
            normalizeStatus(order.status) === "DELIVERED"
        ).length;
}

/* ==============================
   FILTER
============================== */

function filterOrders() {
    const selectedStatus =
        document.getElementById("statusFilter").value;

    if (selectedStatus === "ALL") {
        filteredOrders = [...allOrders];
    } else {
        filteredOrders = allOrders.filter(order =>
            normalizeStatus(order.status) === selectedStatus
        );
    }

    renderOrders();
}

/* ==============================
   RENDER ORDERS
============================== */

function renderOrders() {
    const message = document.getElementById("ordersMessage");
    const tableWrapper = document.getElementById("ordersTableWrapper");
    const tableBody = document.getElementById("ordersTableBody");
    const pagination = document.getElementById("ordersPagination");

    tableBody.replaceChildren();

    if (filteredOrders.length === 0) {
        tableWrapper.hidden = true;
        pagination.hidden = true;
        message.hidden = false;
        message.className = "orders-message";
        message.textContent = allOrders.length === 0
            ? "Bạn chưa có đơn hàng nào."
            : "Không tìm thấy đơn hàng phù hợp.";

        return;
    }

    message.hidden = true;
    tableWrapper.hidden = false;

    const totalPages = Math.ceil(
        filteredOrders.length / ORDERS_PER_PAGE
    );

    const startIndex = (currentPage - 1) * ORDERS_PER_PAGE;

    const pageOrders = filteredOrders.slice(
        startIndex,
        startIndex + ORDERS_PER_PAGE
    );

    pageOrders.forEach(order => {
        const row = document.createElement("tr");

        const orderIdCell = document.createElement("td");
        orderIdCell.className = "order-id";
        orderIdCell.textContent = `#${order.orderId}`;

        const dateCell = document.createElement("td");
        dateCell.textContent = formatDate(order.orderDate);

        const quantityCell = document.createElement("td");
        const itemCount = Array.isArray(order.items)
            ? order.items.reduce(
                (total, item) => total + Number(item.quantity || 0), 0
            )
            : 0;
        quantityCell.textContent = `${itemCount} sản phẩm`;

        const totalCell = document.createElement("td");
        totalCell.textContent = formatCurrency(order.totalAmount);

        const statusCell = document.createElement("td");
        statusCell.appendChild(createStatusBadge(order.status));

        const actionCell = document.createElement("td");
        const detailButton = document.createElement("button");

        detailButton.type = "button";
        detailButton.className = "view-order-btn";
        detailButton.textContent = "Chi tiết";

        detailButton.addEventListener("click", function () {
            showOrderDetail(order);
        });

        actionCell.appendChild(detailButton);

        row.append(
            orderIdCell,
            dateCell,
            quantityCell,
            totalCell,
            statusCell,
            actionCell
        );

        tableBody.appendChild(row);
    });

    pagination.hidden = totalPages <= 1;

    document.getElementById("pageInfo").textContent =
        `Trang ${currentPage} / ${totalPages}`;

    document.getElementById("previousPage").disabled =
        currentPage === 1;

    document.getElementById("nextPage").disabled =
        currentPage === totalPages;
}

/* ==============================
   STATUS
============================== */

function getStatusInfo(status) {
    const statusMap = {
        PENDING: {
            label: "Chờ xác nhận",
            className: "status-pending"
        },
        CONFIRMED: {
            label: "Đã xác nhận",
            className: "status-confirmed"
        },
        PROCESSING: {
            label: "Đang xử lý",
            className: "status-processing"
        },
        SHIPPED: {
            label: "Đang giao",
            className: "status-shipped"
        },
        DELIVERED: {
            label: "Đã giao",
            className: "status-delivered"
        },
        CANCELLED: {
            label: "Đã hủy",
            className: "status-cancelled"
        }
    };

    return statusMap[normalizeStatus(status)] || {
        label: status || "Không xác định",
        className: ""
    };
}

function createStatusBadge(status) {
    const info = getStatusInfo(status);

    const badge = document.createElement("span");
    badge.className = `order-status ${info.className}`;
    badge.textContent = info.label;

    return badge;
}

/* ==============================
   ORDER DETAIL
============================== */

function showOrderDetail(order) {
    const modal = document.getElementById("orderModal");
    const orderId = document.getElementById("detailOrderId");
    const content = document.getElementById("orderDetailContent");

    orderId.textContent = `Mã đơn hàng: #${order.orderId}`;
    content.replaceChildren();

    const infoSection = document.createElement("section");
    infoSection.className = "order-detail-section";

    const infoTitle = document.createElement("h3");
    infoTitle.textContent = "Thông tin đơn hàng";

    const infoBox = document.createElement("div");
    infoBox.className = "order-detail-info";

    const statusRow = document.createElement("p");
    statusRow.append("Trạng thái: ");
    statusRow.appendChild(createStatusBadge(order.status));

    infoBox.append(
        createDetailText("Ngày đặt:", formatDate(order.orderDate)),
        statusRow,
        createDetailText(
            "Địa chỉ giao hàng:",
            order.shippingAddress || "Chưa có thông tin"
        )
    );

    infoSection.append(infoTitle, infoBox);

    const itemsSection = document.createElement("section");
    itemsSection.className = "order-detail-section";

    const itemsTitle = document.createElement("h3");
    itemsTitle.textContent = "Sản phẩm trong đơn hàng";

    const items = Array.isArray(order.items) ? order.items : [];

    const itemsTable = document.createElement("table");
    itemsTable.className = "order-items-table";

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    ["Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"]
        .forEach(label => {
            const th = document.createElement("th");
            th.textContent = label;
            headerRow.appendChild(th);
        });

    thead.appendChild(headerRow);

    const tbody = document.createElement("tbody");

    items.forEach(item => {
        const row = document.createElement("tr");

        [
            item.productName || `Sản phẩm #${item.productId}`,
            item.quantity,
            formatCurrency(item.unitPrice),
            formatCurrency(item.subtotal)
        ].forEach(value => {
            const td = document.createElement("td");
            td.textContent = value ?? "";
            row.appendChild(td);
        });

        tbody.appendChild(row);
    });

    itemsTable.append(thead, tbody);

    itemsSection.append(itemsTitle, itemsTable);

    const totalBox = document.createElement("div");
    totalBox.className = "order-total";

    const totalLabel = document.createElement("span");
    totalLabel.textContent = "Tổng thanh toán";

    const totalValue = document.createElement("span");
    totalValue.textContent = formatCurrency(order.totalAmount);

    totalBox.append(totalLabel, totalValue);

    content.append(infoSection, itemsSection, totalBox);

    modal.hidden = false;
}

function createDetailText(label, value) {
    const paragraph = document.createElement("p");

    const strong = document.createElement("strong");
    strong.textContent = `${label} `;

    const span = document.createElement("span");
    span.textContent = value;

    paragraph.append(strong, span);

    return paragraph;
}

function closeOrderModal() {
    document.getElementById("orderModal").hidden = true;
}

/* ==============================
   FORMAT
============================== */

function formatCurrency(amount) {
    return Number(amount || 0).toLocaleString("vi-VN") + " đ";
}

function formatDate(dateValue) {
    if (!dateValue) return "Không xác định";

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}
