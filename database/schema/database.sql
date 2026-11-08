
-- =====================================================
-- ONLINE SHOP DATABASE
-- Database: online_shop
-- MySQL 8.x
-- Schema only: no sample data
-- =====================================================

USE online_shop;

-- =====================================================
-- 1. USERS
-- =====================================================
CREATE TABLE `users` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT,
    `full_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(255) DEFAULT NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB;

-- =====================================================
-- 2. ROLES
-- =====================================================
CREATE TABLE `roles` (
    `role_id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(30) NOT NULL,
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB;

-- =====================================================
-- 3. CATEGORIES
-- =====================================================
CREATE TABLE `categories` (
    `category_id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    PRIMARY KEY (`category_id`)
) ENGINE=InnoDB;

-- =====================================================
-- 4. PRODUCT MODELS
-- =====================================================
CREATE TABLE `product_models` (
    `product_model_id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `image_url` VARCHAR(255) DEFAULT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`product_model_id`)
) ENGINE=InnoDB;

-- =====================================================
-- 5. PRODUCTS
-- =====================================================
CREATE TABLE `products` (
    `product_id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_name` VARCHAR(255) DEFAULT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `price` DECIMAL(38,2) DEFAULT NULL,
    `stock_quantity` INT NOT NULL DEFAULT 0,
    `image_url` VARCHAR(255) DEFAULT NULL,
    `category_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    `variant_name` VARCHAR(255) DEFAULT NULL,
    `product_model_id` BIGINT DEFAULT NULL,
    PRIMARY KEY (`product_id`),
    KEY `category_id` (`category_id`),
    KEY `FK6qk02g99p07iwehcount25i4h` (`product_model_id`),
    CONSTRAINT `FK6qk02g99p07iwehcount25i4h`
        FOREIGN KEY (`product_model_id`)
        REFERENCES `product_models` (`product_model_id`),
    CONSTRAINT `products_ibfk_1`
        FOREIGN KEY (`category_id`)
        REFERENCES `categories` (`category_id`),
    CONSTRAINT `products_chk_1` CHECK (`price` >= 0),
    CONSTRAINT `products_chk_2` CHECK (`stock_quantity` >= 0)
) ENGINE=InnoDB;

-- =====================================================
-- 6. USER ROLES
-- =====================================================
CREATE TABLE `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `role_id` (`role_id`),
    CONSTRAINT `user_roles_ibfk_1`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT `user_roles_ibfk_2`
        FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB;

-- =====================================================
-- 7. CARTS
-- =====================================================
CREATE TABLE `carts` (
    `cart_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    PRIMARY KEY (`cart_id`),
    UNIQUE KEY `user_id` (`user_id`),
    CONSTRAINT `carts_ibfk_1`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- =====================================================
-- 8. ORDERS
-- =====================================================
CREATE TABLE `orders` (
    `order_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `order_date` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `total_amount` DECIMAL(38,2) DEFAULT NULL,
    `status` VARCHAR(255) DEFAULT NULL,
    `shipping_address` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`order_id`),
    KEY `user_id` (`user_id`),
    CONSTRAINT `orders_ibfk_1`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT `orders_chk_1` CHECK (`total_amount` >= 0)
) ENGINE=InnoDB;

-- =====================================================
-- 9. CART ITEMS
-- =====================================================
CREATE TABLE `cart_items` (
    `cart_item_id` BIGINT NOT NULL AUTO_INCREMENT,
    `cart_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    PRIMARY KEY (`cart_item_id`),
    UNIQUE KEY `cart_id` (`cart_id`, `product_id`),
    KEY `product_id` (`product_id`),
    CONSTRAINT `cart_items_ibfk_1`
        FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`),
    CONSTRAINT `cart_items_ibfk_2`
        FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
    CONSTRAINT `cart_items_chk_1` CHECK (`quantity` > 0)
) ENGINE=InnoDB;

-- =====================================================
-- 10. ORDER ITEMS
-- =====================================================
CREATE TABLE `order_items` (
    `order_item_id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(38,2) DEFAULT NULL,
    PRIMARY KEY (`order_item_id`),
    KEY `order_id` (`order_id`),
    KEY `product_id` (`product_id`),
    CONSTRAINT `order_items_ibfk_1`
        FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
    CONSTRAINT `order_items_ibfk_2`
        FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
    CONSTRAINT `order_items_chk_1` CHECK (`quantity` > 0),
    CONSTRAINT `order_items_chk_2` CHECK (`unit_price` >= 0)
) ENGINE=InnoDB;
-- =========================================
-- TABLE: order_status_history
-- =========================================

CREATE TABLE IF NOT EXISTS order_status_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(255),
    new_status VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (history_id),

    CONSTRAINT fk_order_status_history
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
);