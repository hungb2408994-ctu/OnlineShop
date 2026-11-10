USE online_shop;

START TRANSACTION;

UPDATE products
SET status = 'INACTIVE'
WHERE product_id IN (101, 202, 303, 304, 307)
  AND status = 'ACTIVE';

SELECT
    product_id,
    product_name,
    price,
    status
FROM products
WHERE product_id IN (101, 202, 303, 304, 307, 376)
ORDER BY product_id;

COMMIT;