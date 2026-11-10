
document.addEventListener("DOMContentLoaded", function () {

    const productDetail = document.getElementById("productDetail");

    if (!productDetail) {
        return;
    }

    // Thông tin phiên bản đang được chọn
    let productId = Number(productDetail.dataset.productId);
    let stock = Number(productDetail.dataset.stock);

    const quantityInput = document.getElementById("quantityInput");
    const decreaseButton = document.getElementById("decreaseButton");
    const increaseButton = document.getElementById("increaseButton");
    const addToCartButton = document.getElementById("addToCartButton");
    const cartMessage = document.getElementById("cartMessage");

    const productPrice = document.getElementById("productPrice");
    const productStock = document.getElementById("productStock");
    const maxStock = document.getElementById("maxStock");
    const productImage = document.getElementById("productImage");
    const productImageFallback = document.getElementById("productImageFallback");

    const variantButtons = document.querySelectorAll(".variant-option");

    const originalButtonText = addToCartButton.textContent.trim();

    function showMessage(message, type) {
        cartMessage.textContent = message;
        cartMessage.className = "cart-message " + type;
    }

    function updateQuantityButtons() {
        const quantity = Number(quantityInput.value);

        decreaseButton.disabled = quantity <= 1;
        increaseButton.disabled = quantity >= stock || stock <= 0;

        addToCartButton.disabled = stock <= 0;
    }

    function updateProductImage(imageUrl, altText) {
        if (imageUrl && imageUrl.trim() !== "") {
            if (productImage) {
                productImage.src = imageUrl;
                productImage.alt = altText;
                productImage.style.display = "block";
            } else if (productImageFallback) {
                productImageFallback.src = imageUrl;
                productImageFallback.alt = altText;
                productImageFallback.style.display = "block";
            }
        } else {
            if (productImage) {
                productImage.style.display = "none";
            }

            if (productImageFallback) {
                productImageFallback.style.display = "block";
                productImageFallback.src =
                    "https://placehold.co/500x400?text=No+Image";
                productImageFallback.alt = "No image";
            }
        }
    }

    // Chọn phiên bản sản phẩm
    variantButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            productId = Number(button.dataset.productId);
            stock = Number(button.dataset.stock);

            const price = Number(button.dataset.price);
            const imageUrl = button.dataset.imageUrl;
            const variantName = button.dataset.variantName;

            // Cập nhật phiên bản đang được chọn
            variantButtons.forEach(function (item) {
                item.classList.remove("active");
            });

            button.classList.add("active");

            // Cập nhật dữ liệu sản phẩm
            productDetail.dataset.productId = productId;
            productDetail.dataset.stock = stock;
            productDetail.dataset.price = price;

            // Cập nhật giá
            productPrice.textContent = new Intl.NumberFormat("vi-VN")
                .format(price);

            // Cập nhật tồn kho
            productStock.textContent = stock;
            maxStock.textContent = stock;

            // Cập nhật hình ảnh
            updateProductImage(imageUrl, variantName);

            // Cập nhật giới hạn số lượng
            quantityInput.max = stock;
            quantityInput.value = stock > 0 ? 1 : 0;

            // Nếu hết hàng thì thông báo
            if (stock <= 0) {
                quantityInput.value = 0;
                showMessage("Phiên bản này hiện đã hết hàng.", "error");
            } else {
                cartMessage.textContent = "";
                cartMessage.className = "cart-message";
            }

            updateQuantityButtons();
        });
    });

    // Giảm số lượng
    decreaseButton.addEventListener("click", function () {

        let quantity = Number(quantityInput.value);

        if (quantity > 1) {
            quantity--;
            quantityInput.value = quantity;
        }

        updateQuantityButtons();
    });

    // Tăng số lượng
    increaseButton.addEventListener("click", function () {

        let quantity = Number(quantityInput.value);

        if (quantity < stock) {
            quantity++;
            quantityInput.value = quantity;
        }

        updateQuantityButtons();
    });

    // Khởi tạo trạng thái ban đầu
    updateQuantityButtons();

    // Thêm phiên bản đang chọn vào giỏ hàng
    addToCartButton.addEventListener("click", async function () {

        const token = localStorage.getItem("accessToken");

        if (!token) {
            alert("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng.");
            window.location.href = "/login";
            return;
        }

        const quantity = Number(quantityInput.value);

        if (!Number.isInteger(quantity) || quantity < 1) {
            showMessage("Số lượng sản phẩm không hợp lệ.", "error");
            return;
        }

        if (quantity > stock) {
            showMessage("Số lượng vượt quá số sản phẩm còn trong kho.", "error");
            return;
        }

        if (stock <= 0) {
            showMessage("Phiên bản này hiện đã hết hàng.", "error");
            return;
        }

        addToCartButton.disabled = true;
        addToCartButton.textContent = "Đang thêm sản phẩm...";

        try {

            // Lấy thông tin tài khoản đang đăng nhập
            const userResponse = await fetch("/api/users/me", {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + token
                }
            });

            if (userResponse.status === 401 || userResponse.status === 403) {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("currentUser");

                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                window.location.href = "/login";
                return;
            }

            if (!userResponse.ok) {
                throw new Error("Không thể lấy thông tin tài khoản.");
            }

            const user = await userResponse.json();

            // Gửi đúng ID của phiên bản đang được chọn
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

            if (cartResponse.status === 401 || cartResponse.status === 403) {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("currentUser");

                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                window.location.href = "/login";
                return;
            }

            const responseData = await cartResponse.json().catch(() => null);

            if (!cartResponse.ok) {
                throw new Error(
                    responseData?.message ||
                    responseData?.error ||
                    "Không thể thêm sản phẩm vào giỏ hàng."
                );
            }

            showMessage(
                "Đã thêm sản phẩm vào giỏ hàng thành công!",
                "success"
            );

        } catch (error) {

            console.error("Add to cart error:", error);

            showMessage(
                error.message || "Đã xảy ra lỗi khi thêm vào giỏ hàng.",
                "error"
            );

        } finally {

            addToCartButton.textContent = originalButtonText;
            updateQuantityButtons();
        }
    });
});
