
USE online_shop;

-- Thêm trạng thái sản phẩm.
-- Các bản ghi hiện tại mặc định vẫn được hiển thị.
ALTER TABLE products
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
AFTER product_model_id;

-- Chỉ chấp nhận hai trạng thái hợp lệ.
ALTER TABLE products
ADD CONSTRAINT chk_products_status
CHECK (status IN ('ACTIVE', 'INACTIVE'));

-- Kiểm tra kết quả
SELECT
    status,
    COUNT(*) AS total_products
FROM products
GROUP BY status;
