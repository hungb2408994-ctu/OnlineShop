
USE online_shop;

-- 1. Thống kê tổng số sản phẩm và dòng sản phẩm
SELECT
    (SELECT COUNT(*) FROM products) AS total_products,
    (SELECT COUNT(*) FROM product_models) AS total_product_models,
    (SELECT COUNT(*) FROM products WHERE product_model_id IS NOT NULL) AS linked_products,
    (SELECT COUNT(*) FROM products WHERE product_model_id IS NULL) AS unlinked_products;

-- 2. Liệt kê tất cả dòng sản phẩm và các phiên bản
SELECT
    pm.product_model_id,
    pm.product_name AS model_name,
    pm.category_id AS model_category_id,
    COUNT(p.product_id) AS variant_count,
    GROUP_CONCAT(
        CONCAT(
            'ID=', p.product_id,
            ', Variant=', COALESCE(p.variant_name, 'NULL'),
            ', Price=', COALESCE(p.price, 0),
            ', Stock=', p.stock_quantity
        )
        ORDER BY p.product_id
        SEPARATOR ' | '
    ) AS variants
FROM product_models pm
LEFT JOIN products p
    ON p.product_model_id = pm.product_model_id
GROUP BY
    pm.product_model_id,
    pm.product_name,
    pm.category_id
ORDER BY pm.product_model_id;

-- 3. Tìm các sản phẩm chưa được liên kết với dòng sản phẩm
SELECT
    p.product_id,
    p.product_name,
    p.variant_name,
    p.category_id,
    p.price,
    p.stock_quantity,
    p.image_url
FROM products p
WHERE p.product_model_id IS NULL
ORDER BY p.product_name, p.product_id;

-- 4. Tìm tên sản phẩm xuất hiện nhiều lần
-- Đây chỉ là danh sách nghi vấn, không khẳng định các bản ghi là trùng SKU.
SELECT
    LOWER(TRIM(product_name)) AS normalized_name,
    COUNT(*) AS record_count,
    GROUP_CONCAT(product_id ORDER BY product_id) AS product_ids,
    GROUP_CONCAT(DISTINCT price ORDER BY price) AS prices,
    GROUP_CONCAT(DISTINCT category_id ORDER BY category_id) AS category_ids
FROM products
GROUP BY LOWER(TRIM(product_name))
HAVING COUNT(*) > 1
ORDER BY record_count DESC, normalized_name;

-- 5. Kiểm tra sản phẩm được tham chiếu trong giỏ hàng
SELECT
    p.product_id,
    p.product_name,
    COUNT(DISTINCT ci.cart_item_id) AS cart_references
FROM products p
LEFT JOIN cart_items ci
    ON ci.product_id = p.product_id
GROUP BY p.product_id, p.product_name
HAVING cart_references > 0
ORDER BY p.product_id;

-- 6. Kiểm tra sản phẩm được tham chiếu trong đơn hàng
SELECT
    p.product_id,
    p.product_name,
    COUNT(DISTINCT oi.order_item_id) AS order_references
FROM products p
LEFT JOIN order_items oi
    ON oi.product_id = p.product_id
GROUP BY p.product_id, p.product_name
HAVING order_references > 0
ORDER BY p.product_id;

-- 7. Kiểm tra phiên bản có tên trống hoặc chưa có thông tin
SELECT
    product_id,
    product_name,
    variant_name,
    product_model_id,
    price,
    stock_quantity
FROM products
WHERE product_model_id IS NOT NULL
  AND (
      variant_name IS NULL
      OR TRIM(variant_name) = ''
  )
ORDER BY product_model_id, product_id;

-- 8. Kiểm tra sản phẩm và dòng sản phẩm có category không khớp
SELECT
    p.product_id,
    p.product_name,
    p.category_id AS product_category_id,
    pm.product_model_id,
    pm.product_name AS model_name,
    pm.category_id AS model_category_id
FROM products p
JOIN product_models pm
    ON pm.product_model_id = p.product_model_id
WHERE p.category_id <> pm.category_id
ORDER BY p.product_id;
