
const API_BASE = "/api";

let currentUserId = null;
let currentCart = null;

const checkoutForm = document.getElementById("checkoutForm");
const checkoutItems = document.getElementById("checkoutItems");
const checkoutMessage = document.getElementById("checkoutMessage");
const placeOrderButton = document.getElementById("placeOrderButton");

function formatCurrency(amount) {
    return new Intl.NumberFormat("vi-VN").format(amount) + " ₫";
}

function showMessage(message, type = "error") {
    checkoutMessage.textContent = message;
    checkoutMessage.className = "message " + type;
}

async function apiRequest(url, options = {}) {

    const token = getAccessToken();

    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token,
            ...options.headers
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
            data.message || "Không thể thực hiện yêu cầu."
        );
    }

    return data;
}

async function loadCheckout() {

    try {

        const token = getAccessToken();

        if (!token) {
            redirectToLogin(false);
            return;
        }

        // Lấy thông tin người dùng
        const user = await apiRequest(API_BASE + "/users/me");

        currentUserId = user.userId;

        document.getElementById("fullName").value =
            user.fullName || "";

        document.getElementById("email").value =
            user.email || "";

        // Lấy giỏ hàng
        currentCart = await apiRequest(
            API_BASE + "/cart/" + currentUserId
        );

        renderCheckoutItems(currentCart);

    } catch (error) {

        showMessage(error.message);
        placeOrderButton.disabled = true;

    }
}

function renderCheckoutItems(cart) {

    const items = cart.items || [];

    if (items.length === 0) {

        checkoutItems.textContent = "Giỏ hàng đang trống.";

        placeOrderButton.disabled = true;

        showMessage(
            "Giỏ hàng trống. Vui lòng quay lại chọn sản phẩm.",
            "error"
        );

        return;
    }

    checkoutItems.innerHTML = "";

    let totalQuantity = 0;

    items.forEach(item => {

        totalQuantity += item.quantity;

        const row = document.createElement("div");
        row.className = "checkout-item";

        const name = document.createElement("div");
        name.className = "checkout-item-name";
        name.textContent =
            item.productName + " × " + item.quantity;

        const price = document.createElement("div");
        price.className = "checkout-item-price";
        price.textContent = formatCurrency(item.subtotal);

        row.append(name, price);

        checkoutItems.appendChild(row);

    });

    document.getElementById("totalQuantity").textContent =
        totalQuantity;

    document.getElementById("totalAmount").textContent =
        formatCurrency(cart.totalAmount);

}

checkoutForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const shippingAddress =
        document.getElementById("shippingAddress").value.trim();

    if (!shippingAddress) {
        showMessage("Vui lòng nhập địa chỉ giao hàng.");
        return;
    }

    if (!currentUserId || !currentCart ||
        !currentCart.items || currentCart.items.length === 0) {

        showMessage("Giỏ hàng không có sản phẩm để đặt hàng.");
        return;
    }

    placeOrderButton.disabled = true;
    placeOrderButton.textContent = "Đang xử lý đơn hàng...";

    try {

        const order = await apiRequest(
            API_BASE + "/orders/checkout/" + currentUserId,
            {
                method: "POST",
                body: JSON.stringify({
                    shippingAddress: shippingAddress
                })
            }
        );

        showMessage(
            "Đặt hàng thành công! Mã đơn hàng: #" + order.orderId,
            "success"
        );

        placeOrderButton.textContent = "Đặt hàng thành công";

        // Chuyển đến lịch sử đơn hàng sau khi đặt thành công
        setTimeout(() => {
            window.location.href = "/orders";
        }, 1500);

    } catch (error) {

        showMessage(error.message);

        placeOrderButton.disabled = false;
        placeOrderButton.textContent = "Xác nhận đặt hàng";

    }

});

loadCheckout();
