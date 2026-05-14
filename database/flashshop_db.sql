-- =====================================================
-- FlashShop — full schema + sample data (fresh install)
-- MySQL 8.0+ / MariaDB 10.5+
--
-- This script DROPS database `flashshop_db` if it exists, then recreates it.
-- BACK UP production data before running.
--
-- Default seeded logins (plaintext → bcrypt in DB):
--   Admin:  admin@flashshop.com  /  password
--   Users:  alex@example.com etc. /  password  (same hash for all sample users)
-- =====================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS `flashshop_db`;

CREATE DATABASE `flashshop_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `flashshop_db`;

-- =====================================================
-- 1. ADMIN TABLE
-- =====================================================
CREATE TABLE `admin` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `role` ENUM('super_admin','admin','moderator') NOT NULL DEFAULT 'admin',
  `avatar_url` VARCHAR(500) DEFAULT NULL,
  `reset_token` VARCHAR(255) DEFAULT NULL,
  `reset_expiry` DATETIME DEFAULT NULL,
  `auth_token` VARCHAR(255) DEFAULT NULL,
  `token_expiry` DATETIME DEFAULT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `last_login` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_email` (`email`),
  UNIQUE KEY `uk_admin_username` (`username`),
  KEY `idx_admin_token` (`auth_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. USERS TABLE
-- =====================================================
CREATE TABLE `users` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `avatar_url` VARCHAR(500) DEFAULT NULL,
  `address` TEXT DEFAULT NULL,
  `city` VARCHAR(100) DEFAULT NULL,
  `province` VARCHAR(100) DEFAULT NULL,
  `zip_code` VARCHAR(10) DEFAULT NULL,
  `country` VARCHAR(50) DEFAULT 'Philippines',
  `membership` ENUM('standard','silver','gold','platinum') NOT NULL DEFAULT 'standard',
  `coins` INT UNSIGNED NOT NULL DEFAULT 0,
  `points` INT UNSIGNED NOT NULL DEFAULT 0,
  `auth_token` VARCHAR(255) DEFAULT NULL,
  `token_expiry` DATETIME DEFAULT NULL,
  `email_verified` TINYINT(1) NOT NULL DEFAULT 0,
  `verification_code` VARCHAR(10) DEFAULT NULL,
  `reset_token` VARCHAR(255) DEFAULT NULL,
  `reset_expiry` DATETIME DEFAULT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `last_login` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_token` (`auth_token`),
  KEY `idx_users_membership` (`membership`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. CATEGORIES TABLE
-- =====================================================
CREATE TABLE `categories` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `icon` VARCHAR(50) NOT NULL DEFAULT 'category',
  `color` VARCHAR(7) NOT NULL DEFAULT '#6750A4',
  `image_url` VARCHAR(500) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_categories_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. PRODUCTS TABLE
-- =====================================================
CREATE TABLE `products` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id` INT UNSIGNED NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `discount_percent` INT DEFAULT 0,
  `image_url` VARCHAR(500) NOT NULL,
  `image_url_2` VARCHAR(500) DEFAULT NULL,
  `image_url_3` VARCHAR(500) DEFAULT NULL,
  `image_url_4` VARCHAR(500) DEFAULT NULL,
  `image_url_5` VARCHAR(500) DEFAULT NULL,
  `stock` INT UNSIGNED NOT NULL DEFAULT 0,
  `sold_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `rating` DECIMAL(2,1) NOT NULL DEFAULT 0.0,
  `review_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `brand` VARCHAR(100) DEFAULT NULL,
  `tags` VARCHAR(500) DEFAULT NULL,
  `variations` JSON DEFAULT NULL,
  `is_featured` TINYINT(1) NOT NULL DEFAULT 0,
  `is_flash_sale` TINYINT(1) NOT NULL DEFAULT 0,
  `flash_sale_end` DATETIME DEFAULT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_products_category` (`category_id`),
  KEY `idx_products_featured` (`is_featured`),
  KEY `idx_products_flash` (`is_flash_sale`),
  KEY `idx_products_rating` (`rating`),
  KEY `idx_products_price` (`price`),
  FULLTEXT KEY `ft_products_search` (`name`, `description`, `brand`, `tags`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. CART TABLE
-- =====================================================
CREATE TABLE `cart` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `product_id` INT UNSIGNED NOT NULL,
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1,
  `variation` VARCHAR(100) DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user_product` (`user_id`, `product_id`, `variation`),
  KEY `idx_cart_user` (`user_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. WISHLIST TABLE
-- =====================================================
CREATE TABLE `wishlist` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `product_id` INT UNSIGNED NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wishlist_user_product` (`user_id`, `product_id`),
  KEY `idx_wishlist_user` (`user_id`),
  CONSTRAINT `fk_wishlist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wishlist_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. ORDERS TABLE
-- Admin app maps UI status "completed" → DB value `delivered` (see update_order_status.php).
-- =====================================================
CREATE TABLE `orders` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `order_number` VARCHAR(30) NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  `shipping_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `total` DECIMAL(10,2) NOT NULL,
  `payment_method` ENUM('gcash','credit_card','cod') NOT NULL DEFAULT 'cod',
  `payment_status` ENUM('pending','paid','failed','refunded') NOT NULL DEFAULT 'pending',
  `order_status` ENUM('pending','confirmed','processing','shipped','delivered','cancelled','returned') NOT NULL DEFAULT 'pending',
  `shipping_name` VARCHAR(100) NOT NULL,
  `shipping_phone` VARCHAR(20) NOT NULL,
  `shipping_address` TEXT NOT NULL,
  `shipping_city` VARCHAR(100) DEFAULT NULL,
  `shipping_province` VARCHAR(100) DEFAULT NULL,
  `shipping_zip` VARCHAR(10) DEFAULT NULL,
  `voucher_code` VARCHAR(50) DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `shipped_at` DATETIME DEFAULT NULL,
  `delivered_at` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_number` (`order_number`),
  KEY `idx_orders_user` (`user_id`),
  KEY `idx_orders_status` (`order_status`),
  KEY `idx_orders_payment` (`payment_status`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. ORDER_ITEMS TABLE
-- =====================================================
CREATE TABLE `order_items` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` INT UNSIGNED NOT NULL,
  `product_id` INT UNSIGNED NOT NULL,
  `product_name` VARCHAR(200) NOT NULL,
  `product_image` VARCHAR(500) NOT NULL,
  `variation` VARCHAR(100) DEFAULT NULL,
  `quantity` INT UNSIGNED NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL,
  `total_price` DECIMAL(10,2) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_items_order` (`order_id`),
  KEY `idx_order_items_product` (`product_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. REVIEWS TABLE
-- =====================================================
CREATE TABLE `reviews` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `product_id` INT UNSIGNED NOT NULL,
  `order_id` INT UNSIGNED DEFAULT NULL,
  `rating` TINYINT UNSIGNED NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
  `comment` TEXT DEFAULT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `is_approved` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reviews_product` (`product_id`),
  KEY `idx_reviews_user` (`user_id`),
  CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. BANNERS TABLE
-- =====================================================
CREATE TABLE `banners` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `image_url` VARCHAR(500) NOT NULL,
  `link_type` ENUM('product','category','url','none') NOT NULL DEFAULT 'none',
  `link_value` VARCHAR(500) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `starts_at` DATETIME DEFAULT NULL,
  `ends_at` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_banners_active` (`is_active`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. NOTIFICATIONS TABLE
-- =====================================================
CREATE TABLE `notifications` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `message` TEXT NOT NULL,
  `type` ENUM('order','promo','system','review') NOT NULL DEFAULT 'system',
  `reference_id` INT UNSIGNED DEFAULT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user` (`user_id`, `is_read`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. PASSWORD RESETS (forgot password / reset_password_api)
-- =====================================================
CREATE TABLE `password_resets` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL,
  `token` VARCHAR(64) NOT NULL,
  `account_type` ENUM('user','admin') NOT NULL DEFAULT 'user',
  `user_id` INT UNSIGNED NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_password_resets_token` (`token`),
  KEY `idx_password_resets_email` (`email`),
  KEY `idx_password_resets_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SAMPLE DATA
-- Password hash: bcrypt for plaintext "password" (PHP password_hash / Laravel-style demo hash)
-- To set your own: in PHP run password_hash('YourPass', PASSWORD_BCRYPT) and UPDATE admin/users.
-- =====================================================

INSERT INTO `admin` (`username`, `email`, `password`, `full_name`, `role`) VALUES
('superadmin', 'admin@flashshop.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System Admin', 'super_admin');

INSERT INTO `users` (`full_name`, `email`, `password`, `phone`, `address`, `city`, `province`, `zip_code`, `membership`, `coins`, `points`, `email_verified`) VALUES
('Alex Thompson', 'alex@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+63 917 123 4567', '123 Aurora Boulevard, Barangay Mariana', 'Quezon City', 'Metro Manila', '1112', 'platinum', 850, 4, 1),
('Jane Cooper', 'jane@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+63 918 234 5678', '456 Rizal Avenue', 'Makati City', 'Metro Manila', '1200', 'gold', 500, 2, 1),
('Juan Dela Cruz', 'juan@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+63 919 345 6789', '789 EDSA', 'Pasig City', 'Metro Manila', '1600', 'standard', 100, 1, 1);

INSERT INTO `categories` (`name`, `icon`, `color`, `sort_order`) VALUES
('Electronics', 'devices', '#6750A4', 1),
('Fashion', 'apparel', '#63597C', 2),
('Home', 'home', '#765B00', 3),
('Beauty', 'health_and_safety', '#494551', 4),
('Gaming', 'sports_esports', '#6750A4', 5),
('Groceries', 'grocery', '#63597C', 6),
('Kids', 'toys', '#765B00', 7),
('Sports', 'fitness_center', '#494551', 8);

INSERT INTO `products` (`category_id`, `name`, `description`, `price`, `original_price`, `discount_percent`, `image_url`, `image_url_2`, `stock`, `sold_count`, `rating`, `review_count`, `brand`, `variations`, `is_featured`, `is_flash_sale`) VALUES
(1, 'Premium Noise Canceling Headphones', 'Experience studio-quality sound with our premium noise canceling headphones. Features advanced ANC technology, 40-hour battery life, and ultra-comfortable memory foam ear cups.', 299.00, 399.00, 25, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500', 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=500', 42, 2500, 4.8, 2400, 'TechGadget', '["Space Gray", "Midnight Black", "Pearl White"]', 1, 0),
(1, 'Ultra Slim Smart Watch Pro', 'Stay connected with our Ultra Slim Smart Watch. Tracks fitness, heart rate, sleep and more. Water resistant to 50m with a stunning AMOLED display.', 159.00, 249.00, 36, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500', 'https://images.unsplash.com/photo-1546868871-af0de0ae72be?w=500', 85, 5100, 4.7, 3200, 'TechGadget', '["Silver Edition", "Rose Gold", "Onyx"]', 1, 0),
(2, 'UltraBoost Limited Edition Athletic Running Shoes', 'Engineered for the elite runner. Carbon-Air mesh with responsive ultra-foam technology for an unparalleled running experience. Dynamic arch support system.', 199.00, 289.00, 31, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500', 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=500', 30, 1200, 4.9, 890, 'FlashSport', '["US 8", "US 9", "US 10", "US 11", "US 12"]', 1, 0),
(2, 'Handcrafted Genuine Leather Minimalist Sneakers', 'Premium handcrafted sneakers made from genuine Italian leather. Minimalist design meets ultimate comfort with memory foam insoles.', 110.00, 150.00, 27, 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500', NULL, 65, 450, 5.0, 320, 'FlashSport', '["US 7", "US 8", "US 9", "US 10", "US 11"]', 1, 0),
(1, 'Pro Sound Noise Cancelling Headphones - Onyx Black', 'Professional-grade over-ear headphones with studio-quality sound reproduction and premium comfort for extended listening sessions.', 249.99, 349.99, 29, 'https://images.unsplash.com/photo-1599669454699-248893623440?w=500', NULL, 100, 2500, 4.7, 1800, 'ProAudio', '["Onyx Black", "Arctic White"]', 1, 0),
(1, 'Instax Mini Retro Instant Film Camera - Pastel Blue', 'Capture moments instantly with this retro-style camera. Easy-to-use design with auto exposure and built-in flash for perfect shots every time.', 68.50, 89.00, 23, 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500', NULL, 200, 800, 4.8, 650, 'InstaCam', '["Pastel Blue", "Blush Pink", "Mint Green"]', 0, 0),
(1, 'Luxury Silver Watch', 'Premium luxury watch with Swiss movement, sapphire crystal and genuine leather strap. Timeless elegance meets modern precision.', 49.99, 99.99, 50, 'https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=500', NULL, 15, 750, 4.6, 500, 'LuxTime', '["Silver", "Gold", "Rose Gold"]', 0, 1),
(2, 'Designer Sunglasses UV400', 'Premium polarized sunglasses with UV400 protection. Lightweight titanium frame for all-day comfort. Includes premium carrying case.', 85.00, 106.00, 20, 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=500', NULL, 8, 900, 4.5, 620, 'SunStyle', '["Black", "Tortoise", "Blue"]', 0, 1),
(1, 'Wireless Bluetooth Headphones - White', 'Premium wireless headphones with 35-hour battery life. Deep bass and crystal-clear highs. Foldable design for easy portability.', 129.00, 199.00, 35, 'https://images.unsplash.com/photo-1484704849700-f032a568e944?w=500', NULL, 50, 3000, 4.4, 2100, 'SoundMax', '["White", "Black", "Red"]', 0, 1),
(1, 'Precision Wireless Mouse', 'Ergonomic wireless mouse with 16000 DPI sensor, customizable buttons, and 80-hour battery. Perfect for work and gaming.', 45.00, 65.00, 31, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500', NULL, 300, 4500, 4.9, 3200, 'TechGadget', NULL, 0, 0),
(1, 'Deep Bass Mini Bluetooth Speaker', 'Portable Bluetooth speaker with 360-degree sound, IPX7 waterproof rating and 12-hour battery life. Perfect for outdoor adventures.', 68.50, 95.00, 28, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500', NULL, 150, 2800, 4.8, 2000, 'SoundMax', '["Black", "Blue", "Red", "Green"]', 0, 0),
(3, 'Minimalist Desk Lamp - LED', 'Modern LED desk lamp with 5 brightness levels, USB charging port, and flexible gooseneck design. Touch-sensitive controls.', 39.99, 59.99, 33, 'https://images.unsplash.com/photo-1507473885765-e6ed057ab524?w=500', NULL, 200, 600, 4.6, 400, 'HomeLight', '["White", "Black", "Wood"]', 0, 0),
(4, 'Premium Skincare Set - Complete Kit', 'Complete skincare routine in one set. Includes cleanser, toner, serum, moisturizer, and sunscreen. For all skin types.', 89.99, 129.99, 31, 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=500', NULL, 75, 320, 4.7, 280, 'GlowCo', NULL, 0, 0),
(5, 'Wireless Gaming Controller - RGB', 'Professional gaming controller with Hall Effect joysticks, RGB lighting, and ultra-low latency wireless. Compatible with PC, Switch, and mobile.', 59.99, 79.99, 25, 'https://images.unsplash.com/photo-1592840496694-26d035b52b48?w=500', NULL, 120, 900, 4.8, 700, 'GamePro', '["Black", "White", "Transparent"]', 0, 0),
(2, 'Vitesse Speedster Z10 Running Shoes', 'Competition-ready running shoes with carbon plate technology. Lightweight responsive foam delivers explosive energy return.', 2499.00, 3499.00, 29, 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=500', NULL, 25, 180, 4.9, 150, 'FlashSport', '["Neon Orange", "Electric Blue", "Shadow Black"]', 1, 0),
(1, 'Aura Smart Watch Series 5', 'Next-gen smartwatch with always-on display, ECG monitoring, blood oxygen sensor, and 7-day battery life.', 1850.00, 2499.00, 26, 'https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?w=500', NULL, 40, 650, 4.8, 500, 'TechGadget', '["Silver Mesh", "Black Sport", "Gold Classic"]', 1, 0);

INSERT INTO `banners` (`title`, `image_url`, `link_type`, `link_value`, `sort_order`) VALUES
('Flash Sale - Up to 50% Off', 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800', 'category', '1', 1),
('New Fashion Collection', 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=800', 'category', '2', 2),
('Gaming Gear Deals', 'https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=800', 'category', '5', 3);

INSERT INTO `reviews` (`user_id`, `product_id`, `rating`, `comment`) VALUES
(2, 1, 5, 'Best headphones I have ever owned. The noise cancellation is incredible and battery lasts forever!'),
(3, 1, 5, 'Amazing sound quality. Worth every penny. Highly recommend!'),
(1, 3, 5, 'Best running shoes I have ever owned. Extremely light and the support is amazing!'),
(2, 3, 4, 'Great shoes for daily running. Very comfortable right out of the box.'),
(1, 2, 5, 'Love this smartwatch! The fitness tracking is very accurate and the display is gorgeous.'),
(3, 5, 4, 'Excellent sound quality for the price. Very comfortable for long listening sessions.');

INSERT INTO `notifications` (`user_id`, `title`, `message`, `type`) VALUES
(1, 'Welcome to FlashShop!', 'Thank you for joining FlashShop. Enjoy exclusive deals and fast delivery!', 'system'),
(1, 'Flash Sale Starting Soon!', 'Get ready! Our biggest flash sale starts in 2 hours. Up to 50% off on electronics!', 'promo'),
(1, 'New Arrivals in Fashion', 'Check out the latest fashion trends. New collection just dropped!', 'promo');
