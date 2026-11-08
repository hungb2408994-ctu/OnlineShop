USE online_shop;

DROP TRIGGER IF EXISTS trg_order_status_history;

DELIMITER $$

CREATE TRIGGER trg_order_status_history
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF NOT (OLD.status <=> NEW.status) THEN
        INSERT INTO order_status_history (
            order_id,
            old_status,
            new_status,
            changed_at
        )
        VALUES (
            NEW.order_id,
            OLD.status,
            NEW.status,
            CURRENT_TIMESTAMP
        );
    END IF;
END$$

DELIMITER ;