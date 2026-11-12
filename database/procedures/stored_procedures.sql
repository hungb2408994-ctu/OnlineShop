-- =============================================
-- DATABASE: online_shop
-- STORED PROCEDURES
-- =============================================

USE online_shop;

-- =============================================
-- PROCEDURE: sp_user_order_summary
-- Purpose: Get order statistics of a specific user
-- =============================================

DROP PROCEDURE IF EXISTS sp_user_order_summary;

DELIMITER $$

CREATE PROCEDURE sp_user_order_summary(IN p_user_id BIGINT)
BEGIN
    SELECT
        COUNT(*) AS total_orders,
        COALESCE(SUM(status = 'PENDING'), 0) AS pending_orders,
        COALESCE(SUM(status = 'CONFIRMED'), 0) AS confirmed_orders,
        COALESCE(SUM(status = 'SHIPPING'), 0) AS shipping_orders,
        COALESCE(SUM(status = 'COMPLETED'), 0) AS completed_orders,
        COALESCE(SUM(status = 'CANCELLED'), 0) AS cancelled_orders,
        COALESCE(
            SUM(
                CASE
                    WHEN status = 'COMPLETED' THEN total_amount
                    ELSE 0
                END
            ),
            0
        ) AS completed_amount
    FROM orders
    WHERE user_id = p_user_id;
END$$

DELIMITER ;


-- =============================================
-- PROCEDURE: sp_search_active_products
-- Purpose:
-- Search and filter active products by:
-- keyword, category, min price and max price.
-- Supports searching by product model name.
-- =============================================

DROP PROCEDURE IF EXISTS sp_search_active_products;

DELIMITER $$

CREATE PROCEDURE sp_search_active_products(
    IN p_keyword VARCHAR(255),
    IN p_category_id BIGINT,
    IN p_min_price DECIMAL(38,2),
    IN p_max_price DECIMAL(38,2)
)
BEGIN
    DECLARE v_keyword VARCHAR(255);

    -- Normalize keyword
    SET v_keyword = NULLIF(TRIM(p_keyword), '');

    -- Validate minimum price
    IF p_min_price IS NOT NULL AND p_min_price < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Minimum price cannot be negative';
    END IF;

    -- Validate maximum price
    IF p_max_price IS NOT NULL AND p_max_price < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Maximum price cannot be negative';
    END IF;

    -- Validate price range
    IF p_min_price IS NOT NULL
       AND p_max_price IS NOT NULL
       AND p_min_price > p_max_price THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Minimum price cannot exceed maximum price';
    END IF;

    SELECT
        p.product_id,
        p.product_name,
        p.description,
        p.price,
        p.stock_quantity,
        p.image_url,
        p.category_id,
        p.created_at,
        p.variant_name,
        p.product_model_id,
        p.status

    FROM products p

    LEFT JOIN product_models pm
        ON p.product_model_id = pm.product_model_id

    WHERE p.status = 'ACTIVE'

      AND (
          v_keyword IS NULL
          OR LOWER(p.product_name)
                LIKE CONCAT('%', LOWER(v_keyword), '%')
          OR LOWER(pm.product_name)
                LIKE CONCAT('%', LOWER(v_keyword), '%')
      )

      AND (
          p_category_id IS NULL
          OR p.category_id = p_category_id
      )

      AND (
          p_min_price IS NULL
          OR p.price >= p_min_price
      )

      AND (
          p_max_price IS NULL
          OR p.price <= p_max_price
      )

    ORDER BY p.product_id;

END$$

DELIMITER ;


-- =============================================
-- PROCEDURE: sp_get_cart_details
-- Purpose:
-- Get cart items with subtotal and total amount.
-- =============================================

DROP PROCEDURE IF EXISTS sp_get_cart_details;

DELIMITER $$

CREATE PROCEDURE sp_get_cart_details(
    IN p_cart_id BIGINT
)
BEGIN
    IF p_cart_id IS NULL OR p_cart_id <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid cart ID';
    END IF;

    SELECT
        p.product_id,
        p.product_name,
        p.price,
        ci.quantity,

        -- Subtotal of each cart item
        (p.price * ci.quantity) AS subtotal,

        -- Total amount of the entire cart
        SUM(p.price * ci.quantity) OVER () AS total_amount

    FROM cart_items ci

    INNER JOIN products p
        ON ci.product_id = p.product_id

    WHERE ci.cart_id = p_cart_id

    ORDER BY ci.cart_item_id;

END$$

DELIMITER ;


-- =============================================
-- PROCEDURE: sp_add_cart_item
-- Purpose:
-- Add a product to cart or increase its quantity.
-- Validate stock and prevent concurrent over-selling.
-- =============================================

DROP PROCEDURE IF EXISTS sp_add_cart_item;

DELIMITER $$

CREATE PROCEDURE sp_add_cart_item(
    IN p_cart_id BIGINT,
    IN p_product_id BIGINT,
    IN p_quantity INT
)
BEGIN
    DECLARE v_stock_quantity INT DEFAULT 0;
    DECLARE v_product_status VARCHAR(20);
    DECLARE v_current_quantity INT DEFAULT 0;
    DECLARE v_product_found BOOLEAN DEFAULT TRUE;
    DECLARE v_cart_exists INT DEFAULT 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_product_found = FALSE;

    -- Validate input
    IF p_cart_id IS NULL OR p_cart_id <= 0
       OR p_product_id IS NULL OR p_product_id <= 0
       OR p_quantity IS NULL OR p_quantity <= 0 THEN

        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid cart, product or quantity';
    END IF;

    -- Check cart existence
    SELECT COUNT(*)
    INTO v_cart_exists
    FROM carts
    WHERE cart_id = p_cart_id;

    IF v_cart_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cart does not exist';
    END IF;

    -- Lock product row to prevent concurrent stock changes
    SELECT stock_quantity, status
    INTO v_stock_quantity, v_product_status
    FROM products
    WHERE product_id = p_product_id
    FOR UPDATE;

    IF v_product_found = FALSE THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product does not exist';
    END IF;

    -- Check product status
    IF v_product_status <> 'ACTIVE' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product is not active';
    END IF;

    -- Get current quantity in cart
    SELECT COALESCE(
        (
            SELECT quantity
            FROM cart_items
            WHERE cart_id = p_cart_id
              AND product_id = p_product_id
            LIMIT 1
        ),
        0
    )
    INTO v_current_quantity;

    -- Check available stock
    IF v_current_quantity + p_quantity > v_stock_quantity THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient product stock';
    END IF;

    -- Insert new item or increase existing quantity
    IF v_current_quantity = 0 THEN

        INSERT INTO cart_items (
            cart_id,
            product_id,
            quantity
        )
        VALUES (
            p_cart_id,
            p_product_id,
            p_quantity
        );

    ELSE

        UPDATE cart_items
        SET quantity = v_current_quantity + p_quantity
        WHERE cart_id = p_cart_id
          AND product_id = p_product_id;

    END IF;

END$$

DELIMITER ;


-- =============================================
-- PROCEDURE: sp_update_cart_item
-- Purpose: Update quantity of an existing cart item
-- =============================================

DROP PROCEDURE IF EXISTS sp_update_cart_item;

DELIMITER $$

CREATE PROCEDURE sp_update_cart_item(
    IN p_cart_id BIGINT,
    IN p_product_id BIGINT,
    IN p_quantity INT
)
BEGIN
    DECLARE v_stock_quantity INT DEFAULT 0;
    DECLARE v_product_status VARCHAR(20);
    DECLARE v_product_found BOOLEAN DEFAULT TRUE;
    DECLARE v_cart_item_count INT DEFAULT 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_product_found = FALSE;

    -- 1. Validate input
    IF p_cart_id IS NULL OR p_cart_id <= 0
       OR p_product_id IS NULL OR p_product_id <= 0
       OR p_quantity IS NULL OR p_quantity <= 0 THEN

        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid cart, product or quantity';
    END IF;

    -- 2. Check whether cart item exists
    SELECT COUNT(*)
    INTO v_cart_item_count
    FROM cart_items
    WHERE cart_id = p_cart_id
      AND product_id = p_product_id;

    IF v_cart_item_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product does not exist in cart';
    END IF;

    -- 3. Lock product and get stock/status
    SELECT stock_quantity, status
    INTO v_stock_quantity, v_product_status
    FROM products
    WHERE product_id = p_product_id
    FOR UPDATE;

    IF v_product_found = FALSE THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product does not exist';
    END IF;

    -- 4. Check product status
    IF v_product_status <> 'ACTIVE' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product is not active';
    END IF;

    -- 5. Check stock
    IF p_quantity > v_stock_quantity THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient product stock';
    END IF;

    -- 6. Update cart item
    UPDATE cart_items
    SET quantity = p_quantity
    WHERE cart_id = p_cart_id
      AND product_id = p_product_id;

END$$

DELIMITER ;


-- =============================================
-- PROCEDURE: sp_remove_cart_item
-- Purpose: Remove a product from a user's cart
-- =============================================

DROP PROCEDURE IF EXISTS sp_remove_cart_item;

DELIMITER $$

CREATE PROCEDURE sp_remove_cart_item(
    IN p_cart_id BIGINT,
    IN p_product_id BIGINT
)
BEGIN
    DECLARE v_cart_item_count INT DEFAULT 0;

    -- 1. Validate input
    IF p_cart_id IS NULL OR p_cart_id <= 0
       OR p_product_id IS NULL OR p_product_id <= 0 THEN

        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid cart or product ID';
    END IF;

    -- 2. Check whether product exists in cart
    SELECT COUNT(*)
    INTO v_cart_item_count
    FROM cart_items
    WHERE cart_id = p_cart_id
      AND product_id = p_product_id;

    IF v_cart_item_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product does not exist in cart';
    END IF;

    -- 3. Remove cart item
    DELETE FROM cart_items
    WHERE cart_id = p_cart_id
      AND product_id = p_product_id;

END$$

DELIMITER ;
