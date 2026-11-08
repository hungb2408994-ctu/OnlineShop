
document.addEventListener("DOMContentLoaded", function () {

    const productDetail = document.getElementById("productDetail");

    const productId = Number(productDetail.dataset.productId);
    const stock = Number(productDetail.dataset.stock);

    const quantityInput = document.getElementById("quantityInput");
    const decreaseButton = document.getElementById("decreaseButton");
    const increaseButton = document.getElementById("increaseButton");
    const addToCartButton = document.getElementById("addToCartButton");
    const cartMessage = document.getElementById("cartMessage");

    function showMessage(message, type) {
        cartMessage.textContent = message;
        cartMessage.className = "cart-message " + type;
    }

    function updateQuantityButtons() {
        const quantity = Number(quantityInput.value);

        decreaseButton.disabled = quantity <= 1;
        increaseButton.disabled = quantity >= stock;
    }

    decreaseButton.addEventListener("click", function () {
        let quantity = Number(quantityInput.value);

        if (quantity > 1) {
            quantity--;
            quantityInput.value = quantity;
        }

        updateQuantityButtons();
    });

    increaseButton.addEventListener("click", function () {
        let quantity = Number(quantityInput.value);

        if (quantity < stock) {
            quantity++;
            quantityInput.value = quantity;
        }

        updateQuantityButtons();
    });

    updateQuantityButtons();

    addToCartButton.addEventListener("click", async function () {

        const token = getAccessToken();

        if (!token) {
            alert("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            redirectToLogin(false);
            return;
        }

        const quantity = Number(quantityInput.value);

        if (!Number.isInteger(quantity) || quantity < 1 || quantity > stock) {
            showMessage("Số lượng sản phẩm không hợp lệ.", "error");
            return;
        }

        addToCartButton.disabled = true;
        addToCartButton.textContent = "Đang thêm...";

        try {

            // Lấy thông tin người dùng hiện tại
            const userResponse = await fetch("/api/users/me", {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + token
                }
            });

            if (userResponse.status === 401 || userResponse.status === 403) {
                clearSession();

                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                redirectToLogin(true);
                return;
            }

            if (!userResponse.ok) {
                throw new Error("Không thể lấy thông tin người dùng.");
            }

            const user = await userResponse.json();

            // Gọi API thêm sản phẩm vào giỏ hàng
            const cartResponse = await fetch(
                `/api/cart/${user.userId}/items`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": "Bearer " + token
                    },
                    body: JSON.stringify({
                        productId: productId,
                        quantity: quantity
                    })
                }
            );

            const responseText = await cartResponse.text();

            if (!cartResponse.ok) {
                throw new Error(
                    responseText || "Không thể thêm sản phẩm vào giỏ hàng."
                );
            }

            showMessage(
                "Đã thêm " + quantity + " sản phẩm vào giỏ hàng thành công!",
                "success"
            );

        } catch (error) {

            console.error("Add to cart error:", error);

            showMessage(
                error.message || "Có lỗi xảy ra khi thêm sản phẩm.",
                "error"
            );

        } finally {

            addToCartButton.disabled = stock <= 0;
            addToCartButton.textContent = "🛒 Thêm vào giỏ hàng";

        }

    });

});
