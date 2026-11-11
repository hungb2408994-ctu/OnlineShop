USE online_shop;

START TRANSACTION;

-- =========================================
-- 1. Tạo model AirPods 4 nếu chưa tồn tại
-- =========================================

INSERT INTO product_models (
    product_name,
    category_id,
    description,
    image_url
)
SELECT
    'AirPods 4',
    3,
    'Apple AirPods 4 - phiên bản tiêu chuẩn và ANC',
    p.image_url
FROM products p
WHERE p.product_id = 360
  AND NOT EXISTS (
      SELECT 1
      FROM product_models pm
      WHERE pm.product_name = 'AirPods 4'
        AND pm.category_id = 3
  );

-- Gán hai phiên bản AirPods 4
UPDATE products
SET
    product_model_id = (
        SELECT MIN(pm.product_model_id)
        FROM product_models pm
        WHERE pm.product_name = 'AirPods 4'
          AND pm.category_id = 3
    ),
    variant_name = CASE product_id
        WHEN 360 THEN 'Tiêu chuẩn'
        WHEN 361 THEN 'ANC'
    END
WHERE product_id IN (360, 361)
  AND status = 'ACTIVE'
  AND product_model_id IS NULL;


-- =========================================
-- 2. Tạo model MacBook Pro nếu chưa tồn tại
-- =========================================

INSERT INTO product_models (
    product_name,
    category_id,
    description,
    image_url
)
SELECT
    'MacBook Pro',
    2,
    'Apple MacBook Pro - các cấu hình chip và kích thước',
    p.image_url
FROM products p
WHERE p.product_id = 337
  AND NOT EXISTS (
      SELECT 1
      FROM product_models pm
      WHERE pm.product_name = 'MacBook Pro'
        AND pm.category_id = 2
  );

-- Gán ba phiên bản MacBook Pro
UPDATE products
SET
    product_model_id = (
        SELECT MIN(pm.product_model_id)
        FROM product_models pm
        WHERE pm.product_name = 'MacBook Pro'
          AND pm.category_id = 2
    ),
    variant_name = CASE product_id
        WHEN 337 THEN 'M4 - 14 inch'
        WHEN 338 THEN 'M4 Pro - 14 inch'
        WHEN 339 THEN 'M4 Max - 16 inch'
    END
WHERE product_id IN (337, 338, 339)
  AND status = 'ACTIVE'
  AND product_model_id IS NULL;


-- =========================================
-- 3. Kiểm tra kết quả
-- =========================================

SELECT
    p.product_id,
    p.product_name,
    p.variant_name,
    p.product_model_id,
    pm.product_name AS model_name,
    p.price,
    p.stock_quantity,
    p.status
FROM products p
LEFT JOIN product_models pm
    ON p.product_model_id = pm.product_model_id
WHERE p.product_id IN (360, 361, 337, 338, 339)
ORDER BY p.product_model_id, p.product_id;

COMMIT;