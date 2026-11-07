
const API_BASE = "/api";

const cartItemsElement = document.getElementById("cartItems");
const cartContent = document.getElementById("cartContent");
const emptyCart = document.getElementById("emptyCart");
const cartMessage = document.getElementById("cartMessage");

let currentUserId = null;

function formatCurrency(amount) {
    return new Intl.NumberFormat("vi-VN").format(amount) + " ₫";
}

function showMessage(message, type = "error") {
    cartMessage.textContent = message;
    cartMessage.className = "cart-message " + type;
}

async function apiRequest(url, options = {}) {
    const token = localStorage.getItem("accessToken");

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
            localStorage.removeItem("accessToken");
            localStorage.removeItem("currentUser");
            window.location.href = "/login";
            throw new Error("Phiên đăng nhập đã hết hạn.");
        }

        throw new Error(data.message || "Không thể thực hiện yêu cầu.");
    }

    return data;
}

async function loadCart() {
    try {
        const token = localStorage.getItem("accessToken");

        if (!token) {
            window.location.href = "/login";
            return;
        }

        const user = await apiRequest(API_BASE + "/users/me");
        currentUserId = user.userId;

        const cart = await apiRequest(API_BASE + "/cart/" + currentUserId);

        renderCart(cart);

    } catch (error) {
        showMessage(error.message);
    }
}

function renderCart(cart) {
    const items = cart.items || [];

    if (items.length === 0) {
        cartContent.hidden = true;
        emptyCart.hidden = false;
        return;
    }

    cartContent.hidden = false;
    emptyCart.hidden = true;

    cartItemsElement.innerHTML = "";

    let totalQuantity = 0;

    items.forEach(item => {
        totalQuantity += item.quantity;

        const row = document.createElement("div");
        row.className = "cart-item";

        const name = document.createElement("div");
        name.className = "product-name";
        name.textContent = item.productName;

        const price = document.createElement("div");
        price.className = "item-price";
        price.textContent = formatCurrency(item.price);

        const quantity = document.createElement("div");
        quantity.className = "quantity-control";

        const decrease = document.createElement("button");
        decrease.type = "button";
        decrease.textContent = "−";
        decrease.addEventListener("click", () => {
            if (item.quantity > 1) {
                updateQuantity(item.productId, item.quantity - 1);
            }
        });

        const quantityValue = document.createElement("span");
        quantityValue.className = "quantity-value";
        quantityValue.textContent = item.quantity;

        const increase = document.createElement("button");
        increase.type = "button";
        increase.textContent = "+";
        increase.addEventListener("click", () => {
            updateQuantity(item.productId, item.quantity + 1);
        });

        quantity.append(decrease, quantityValue, increase);

        const subtotal = document.createElement("div");
        subtotal.className = "item-subtotal";
        subtotal.textContent = formatCurrency(item.subtotal);

        const remove = document.createElement("button");
        remove.type = "button";
        remove.className = "remove-button";
        remove.textContent = "×";
        remove.title = "Xóa sản phẩm";
        remove.addEventListener("click", () => {
            removeItem(item.productId);
        });

        row.append(name, price, quantity, subtotal, remove);
        cartItemsElement.appendChild(row);
    });

    document.getElementById("totalQuantity").textContent = totalQuantity;
    document.getElementById("totalAmount").textContent =
        formatCurrency(cart.totalAmount);
}

async function updateQuantity(productId, quantity) {
    try {
        await apiRequest(
            API_BASE + "/cart/" + currentUserId + "/items/" + productId,
            {
                method: "PUT",
                body: JSON.stringify({ productId, quantity })
            }
        );

        await loadCart();
        showMessage("Đã cập nhật giỏ hàng.", "success");

    } catch (error) {
        showMessage(error.message);
    }
}

async function removeItem(productId) {
    try {
        await apiRequest(
            API_BASE + "/cart/" + currentUserId + "/items/" + productId,
            { method: "DELETE" }
        );

        await loadCart();
        showMessage("Đã xóa sản phẩm khỏi giỏ hàng.", "success");

    } catch (error) {
        showMessage(error.message);
    }
}

document.getElementById("checkoutButton").addEventListener("click", () => {
    window.location.href = "/checkout";
});

loadCart();
