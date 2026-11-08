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