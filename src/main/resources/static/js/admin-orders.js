
const ORDER_API_URL = "/api/admin/orders";

// Lấy JWT token đã lưu sau khi đăng nhập.
function getAccessToken() {
    return localStorage.getItem("accessToken")
        || localStorage.getItem("token");
}

// Tạo headers dùng chung cho các request Admin.
function getAuthHeaders(includeJson = false) {
    const token = getAccessToken();

    if (!token) {
        throw new Error("Không tìm thấy access token. Vui lòng đăng nhập lại.");
    }

    const headers = {
        "Authorization": `Bearer ${token}`
    };

    if (includeJson) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// Gửi request và xử lý lỗi chung.
async function orderApiRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...getAuthHeaders(
                options.headers?.["Content-Type"] === "application/json"
            ),
            ...options.headers
        }
    });

    if (!response.ok) {
        let message = "Có lỗi xảy ra khi xử lý đơn hàng.";

        try {
            const errorData = await response.json();
            message = errorData.message || message;
        } catch {
            const errorText = await response.text();
            if (errorText) {
                message = errorText;
            }
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        return response.json();
    }

    return null;
}

// Các phần tử giao diện
const orderTableBody = document.getElementById("orderTableBody");

const searchOrderInput = document.getElementById("searchOrderInput");
const statusFilter = document.getElementById("statusFilter");
const resetOrderBtn = document.getElementById("resetOrderBtn");

const orderDetailModal = document.getElementById("orderDetailModal");
const orderDetailContent = document.getElementById("orderDetailContent");

const orderStatusModal = document.getElementById("orderStatusModal");
const orderStatusForm = document.getElementById("orderStatusForm");

let allOrders = [];
let searchTimeout = null;

// Chuyển trạng thái sang tiếng Việt
function getStatusText(status) {
    const statusMap = {
        PENDING: "Chờ xác nhận",
        CONFIRMED: "Đã xác nhận",
        SHIPPING: "Đang giao",
        COMPLETED: "Đã hoàn thành",
        CANCELLED: "Đã hủy"
    };

    return statusMap[status] || status;
}

// Định dạng tiền
function formatCurrency(amount) {
    return Number(amount || 0).toLocaleString("vi-VN") + " đ";
}

// Định dạng ngày
function formatDate(date) {
    if (!date) {
        return "";
    }

    return new Date(date).toLocaleString("vi-VN");
}

// Tạo ô trong bảng, sử dụng textContent để tránh chèn HTML không an toàn.
function createCell(value) {
    const cell = document.createElement("td");
    cell.textContent = value ?? "";
    return cell;
}

// Hiển thị danh sách đơn hàng
function displayOrders(orders) {
    orderTableBody.innerHTML = "";

    if (!Array.isArray(orders) || orders.length === 0) {
        const row = document.createElement("tr");
        const cell = createCell("Không có đơn hàng nào.");
        cell.colSpan = 7;
        row.appendChild(cell);
        orderTableBody.appendChild(row);
        return;
    }

    orders.forEach(order => {
        const row = document.createElement("tr");

        row.appendChild(createCell("#" + order.orderId));
        row.appendChild(createCell(order.userId));
        row.appendChild(createCell(formatDate(order.orderDate)));
        row.appendChild(createCell(formatCurrency(order.totalAmount)));
        row.appendChild(createCell(getStatusText(order.status)));
        row.appendChild(createCell(order.shippingAddress));

        const actionCell = document.createElement("td");
        const actionContainer = document.createElement("div");
        actionContainer.className = "action-buttons";

        const detailButton = document.createElement("button");
        detailButton.textContent = "Chi tiết";
        detailButton.className = "edit-btn";
        detailButton.addEventListener("click", () => {
            viewOrderDetail(order.orderId);
        });

        const statusButton = document.createElement("button");
        statusButton.textContent = "Cập nhật";
        statusButton.className = "order-status-btn";
        statusButton.addEventListener("click", () => {
            openStatusModal(order);
        });

        actionContainer.appendChild(detailButton);
        actionContainer.appendChild(statusButton);

        actionCell.appendChild(actionContainer);
        row.appendChild(actionCell);

        orderTableBody.appendChild(row);
    });
}

// Cập nhật số liệu thống kê từ danh sách hiện tại.
function updateOrderStatistics(orders) {
    document.getElementById("totalOrders").textContent = orders.length;

    document.getElementById("pendingOrders").textContent =
        orders.filter(order => order.status === "PENDING").length;

    document.getElementById("shippingOrders").textContent =
        orders.filter(order => order.status === "SHIPPING").length;

    document.getElementById("completedOrders").textContent =
        orders.filter(order => order.status === "COMPLETED").length;

    document.getElementById("cancelledOrders").textContent =
        orders.filter(order => order.status === "CANCELLED").length;
}

// Hiển thị dữ liệu lên giao diện
function renderOrders(orders) {
    displayOrders(orders);
}

// Tải tất cả đơn hàng
async function loadOrders() {
    try {
        const orders = await orderApiRequest(ORDER_API_URL);

        allOrders = orders;

        displayOrders(orders);
        updateOrderStatistics(orders);

    } catch (error) {
        showOrderError(error.message);
    }
}

// Hiển thị lỗi tải dữ liệu
function showOrderError(message) {
    orderTableBody.innerHTML = "";

    const row = document.createElement("tr");
    const cell = createCell(message);
    cell.colSpan = 7;
    cell.className = "error-message";

    row.appendChild(cell);
    orderTableBody.appendChild(row);
}

// Tìm kiếm đơn hàng
async function searchOrders(keyword) {
    try {
        if (!keyword.trim()) {
            await loadOrders();
            return;
        }

        const url =
            `${ORDER_API_URL}/search?keyword=${encodeURIComponent(keyword)}`;

        const orders = await orderApiRequest(url);
        renderOrders(orders);

    } catch (error) {
        showOrderError(error.message);
    }
}

// Lọc đơn hàng theo trạng thái
async function filterOrdersByStatus(status) {
    try {
        if (!status) {
            await loadOrders();
            return;
        }

        const orders = await orderApiRequest(
            `${ORDER_API_URL}/status/${encodeURIComponent(status)}`
        );

        renderOrders(orders);

    } catch (error) {
        showOrderError(error.message);
    }
}

// Xem chi tiết đơn hàng
async function viewOrderDetail(orderId) {
    try {
        const order = await orderApiRequest(
            `${ORDER_API_URL}/${orderId}`
        );

        displayOrderDetail(order);
        orderDetailModal.classList.remove("hidden");

    } catch (error) {
        alert(error.message);
    }
}

// Hiển thị chi tiết đơn hàng
function displayOrderDetail(order) {
    orderDetailContent.innerHTML = "";

    const information = document.createElement("div");
    information.className = "order-information";

    const details = [
        ["Mã đơn hàng", "#" + order.orderId],
        ["Mã khách hàng", order.userId],
        ["Ngày đặt", formatDate(order.orderDate)],
        ["Tổng tiền", formatCurrency(order.totalAmount)],
        ["Trạng thái", getStatusText(order.status)],
        ["Địa chỉ giao hàng", order.shippingAddress]
    ];

    details.forEach(([label, value]) => {
        const paragraph = document.createElement("p");
        const strong = document.createElement("strong");

        strong.textContent = label + ": ";
        paragraph.appendChild(strong);
        paragraph.appendChild(document.createTextNode(value ?? ""));

        information.appendChild(paragraph);
    });

    const title = document.createElement("h3");
    title.textContent = "Danh sách sản phẩm";
    title.className = "order-items-title";

    const tableContainer = document.createElement("div");
    tableContainer.className = "table-container";

    const table = document.createElement("table");

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    ["Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"]
        .forEach(text => {
            const th = document.createElement("th");
            th.textContent = text;
            headerRow.appendChild(th);
        });

    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    (order.items || []).forEach(item => {
        const row = document.createElement("tr");

        row.appendChild(createCell(item.productId));
        row.appendChild(createCell(item.productName));
        row.appendChild(createCell(item.quantity));
        row.appendChild(createCell(formatCurrency(item.unitPrice)));
        row.appendChild(createCell(formatCurrency(item.subtotal)));

        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    tableContainer.appendChild(table);

    orderDetailContent.appendChild(information);
    orderDetailContent.appendChild(title);
    orderDetailContent.appendChild(tableContainer);
}

// Mở modal cập nhật trạng thái
function openStatusModal(order) {
    if (order.status === "CANCELLED") {
        alert("Đơn hàng này đã bị hủy, không thể cập nhật trạng thái.");
        return;
    }

    document.getElementById("statusOrderId").value = order.orderId;
    document.getElementById("newOrderStatus").value = order.status;

    orderStatusModal.classList.remove("hidden");
}

// Đóng modal cập nhật trạng thái
function closeStatusModal() {
    orderStatusModal.classList.add("hidden");
    orderStatusForm.reset();
}

// Cập nhật trạng thái đơn hàng
async function updateOrderStatus(event) {
    event.preventDefault();

    const orderId = document.getElementById("statusOrderId").value;
    const newStatus = document.getElementById("newOrderStatus").value;

    const currentOrder = allOrders.find(
        order => String(order.orderId) === String(orderId)
    );

    if (!currentOrder) {
        alert("Không tìm thấy đơn hàng.");
        return;
    }

    if (currentOrder.status === newStatus) {
        alert("Trạng thái đơn hàng không thay đổi.");
        return;
    }

    if (
        newStatus === "CANCELLED" &&
        !confirm(
            "Bạn có chắc chắn muốn hủy đơn hàng này?\n" +
            "Số lượng sản phẩm sẽ được hoàn lại vào kho."
        )
    ) {
        return;
    }

    try {
        await orderApiRequest(
            `${ORDER_API_URL}/${orderId}/status`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    status: newStatus
                })
            }
        );

        alert(
            newStatus === "CANCELLED"
                ? "Hủy đơn hàng thành công. Tồn kho đã được hoàn lại."
                : "Cập nhật trạng thái đơn hàng thành công."
        );

        closeStatusModal();
        await loadOrders();

    } catch (error) {
        alert(error.message);
    }
}

// Sự kiện tìm kiếm
searchOrderInput.addEventListener("input", event => {
    clearTimeout(searchTimeout);

    const keyword = event.target.value;

    searchTimeout = setTimeout(() => {
        searchOrders(keyword);
    }, 300);
});

// Sự kiện lọc
statusFilter.addEventListener("change", () => {
    searchOrderInput.value = "";
    filterOrdersByStatus(statusFilter.value);
});

// Làm mới
resetOrderBtn.addEventListener("click", () => {
    searchOrderInput.value = "";
    statusFilter.value = "";
    loadOrders();
});

// Đóng modal chi tiết
document.getElementById("closeOrderDetailBtn")
    .addEventListener("click", () => {
        orderDetailModal.classList.add("hidden");
    });

// Đóng modal trạng thái
document.getElementById("closeStatusModalBtn")
    .addEventListener("click", closeStatusModal);

document.getElementById("cancelStatusBtn")
    .addEventListener("click", closeStatusModal);

// Gửi form cập nhật trạng thái
orderStatusForm.addEventListener("submit", updateOrderStatus);

// Đóng modal khi click ra ngoài
orderDetailModal.addEventListener("click", event => {
    if (event.target === orderDetailModal) {
        orderDetailModal.classList.add("hidden");
    }
});

orderStatusModal.addEventListener("click", event => {
    if (event.target === orderStatusModal) {
        closeStatusModal();
    }
});

// Khởi động
loadOrders();
