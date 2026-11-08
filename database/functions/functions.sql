
-- =============================================
-- DATABASE: online_shop
-- MYSQL FUNCTIONS
-- =============================================

USE online_shop;

-- =============================================
-- FUNCTION 1: fn_order_total
-- Purpose: Calculate total amount of an order
-- =============================================

DROP FUNCTION IF EXISTS fn_order_total;

DELIMITER $$

CREATE FUNCTION fn_order_total(p_order_id BIGINT)
RETURNS DECIMAL(38,2)
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT COALESCE(SUM(quantity * unit_price), 0)
    FROM order_items
    WHERE order_id = p_order_id
)$$

DELIMITER ;


-- =============================================
-- FUNCTION 2: fn_product_sold_quantity
-- Purpose: Calculate ordered quantity of product
-- Exclude CANCELLED orders
-- =============================================

DROP FUNCTION IF EXISTS fn_product_sold_quantity;

DELIMITER $$

CREATE FUNCTION fn_product_sold_quantity(p_product_id BIGINT)
RETURNS BIGINT
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT COALESCE(SUM(oi.quantity), 0)
    FROM order_items oi
    JOIN orders o ON oi.order_id = o.order_id
    WHERE oi.product_id = p_product_id
      AND o.status IN (
          'PENDING',
          'CONFIRMED',
          'SHIPPING',
          'COMPLETED'
      )
)$$

DELIMITER ;
