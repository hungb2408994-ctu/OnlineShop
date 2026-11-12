
const API_BASE = "/api";

const ordersList = document.getElementById("ordersList");
const emptyOrders = document.getElementById("emptyOrders");
const ordersMessage = document.getElementById("ordersMessage");

function formatCurrency(amount) {
    return new Intl.NumberFormat("vi-VN").format(amount) + " ₫";
}

function formatDate(dateString) {
    if (!dateString) return "Chưa xác định";

    return new Date(dateString).toLocaleString("vi-VN");
}

function showError(message) {
    ordersMessage.textContent = message;
    ordersMessage.className = "message error";
}

async function apiRequest(url) {

    const token = getAccessToken();

    const response = await fetch(url, {
        headers: {
            "Authorization": "Bearer " + token
        }
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {

        if (response.status === 401) {
            clearSession();
            redirectToLogin(true);

            throw new Error("Phiên đăng nhập đã hết hạn.");
        }

        throw new Error(
            data.message || "Không thể tải lịch sử đơn hàng."
        );
    }

    return data;
}

function getStatusClass(status) {
    if (status === "DELIVERED" || status === "COMPLETED") {
        return "completed";
    }

    if (status === "CANCELLED") {
        return "cancelled";
    }

    return "";
}

function getStatusText(status) {
    const statuses = {
        PENDING: "Chờ xác nhận",
        CONFIRMED: "Đã xác nhận",
        SHIPPING: "Đang giao hàng",
        DELIVERED: "Đã giao hàng",
        COMPLETED: "Hoàn thành",
        CANCELLED: "Đã hủy"
    };

    return statuses[status] || status;
}

function renderOrders(orders) {

    ordersList.innerHTML = "";

    if (!orders || orders.length === 0) {
        ordersList.hidden = true;
        emptyOrders.hidden = false;
        return;
    }

    ordersList.hidden = false;
    emptyOrders.hidden = true;

    orders.forEach(order => {

        const card = document.createElement("article");
        card.className = "order-card";

        const header = document.createElement("div");
        header.className = "order-header";

        const information = document.createElement("div");

        const code = document.createElement("div");
        code.className = "order-code";
        code.textContent = "Đơn hàng #" + order.orderId;

        const date = document.createElement("div");
        date.className = "order-date";
        date.textContent = "Ngày đặt: " + formatDate(order.orderDate);

        information.append(code, date);

        const status = document.createElement("span");
        status.className =
            "order-status " + getStatusClass(order.status);
        status.textContent = getStatusText(order.status);

        header.append(information, status);

        const products = document.createElement("div");
        products.className = "order-products";

        (order.items || []).forEach(item => {

            const product = document.createElement("div");
            product.className = "order-product";

            const name = document.createElement("span");
            name.className = "order-product-name";
            name.textContent =
                item.productName + " × " + item.quantity;

            const price = document.createElement("span");
            price.className = "order-product-price";
            price.textContent = formatCurrency(item.subtotal);

            product.append(name, price);
            products.appendChild(product);

        });

        const footer = document.createElement("div");
        footer.className = "order-footer";

        const address = document.createElement("div");
        address.className = "shipping-address";
        address.textContent =
            "Địa chỉ giao hàng: " + order.shippingAddress;

        const total = document.createElement("div");
        total.className = "order-total";

        const totalLabel = document.createElement("span");
        totalLabel.textContent = "Tổng thanh toán";

        const totalAmount = document.createElement("strong");
        totalAmount.textContent = formatCurrency(order.totalAmount);

        total.append(totalLabel, totalAmount);
        footer.append(address, total);

        card.append(header, products, footer);
        ordersList.appendChild(card);

    });
}

async function loadOrders() {

    try {

        const token = getAccessToken();

        if (!token) {
            redirectToLogin(false);
            return;
        }

        const user = await apiRequest(API_BASE + "/users/me");

        const orders = await apiRequest(
            API_BASE + "/orders/" + user.userId
        );

        renderOrders(orders);

    } catch (error) {

        console.error(error);
        showError(error.message);

    }
}

loadOrders();
