
document.addEventListener("DOMContentLoaded", async () => {

    const orderId = window.location.pathname.split("/").pop();

    const orderContent = document.getElementById("orderContent");
    const errorMessage = document.getElementById("errorMessage");
    const orderItems = document.getElementById("orderItems");

    try {

        const response = await fetch(`/api/admin/orders/${orderId}`);

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Không thể tải đơn hàng!");
        }

        document.getElementById("orderId").textContent = data.id;
        document.getElementById("userId").textContent = data.userId;
        document.getElementById("orderDate").textContent =
            data.orderDate
                ? new Date(data.orderDate).toLocaleString("vi-VN")
                : "Không có dữ liệu";

        document.getElementById("orderStatus").textContent = data.status;
        document.getElementById("shippingAddress").textContent =
            data.shippingAddress;

        document.getElementById("totalAmount").textContent =
            Number(data.totalAmount).toLocaleString("vi-VN") + " đ";

        orderItems.innerHTML = "";

        data.items.forEach(item => {

            const row = document.createElement("tr");

            const values = [
                item.productId,
                item.productName,
                Number(item.unitPrice).toLocaleString("vi-VN") + " đ",
                item.quantity,
                Number(item.subtotal).toLocaleString("vi-VN") + " đ"
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value ?? "";
                row.appendChild(cell);
            });

            orderItems.appendChild(row);
        });

        orderContent.hidden = false;

    } catch (error) {

        errorMessage.textContent = error.message;

    }

});
