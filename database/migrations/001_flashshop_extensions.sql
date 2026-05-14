-- FlashShop incremental migration — run once on existing database.
-- mysql -u root -p flashshop_db < database/migrations/001_flashshop_extensions.sql
-- If a line errors with "Duplicate column", skip that line (already applied).

USE `flashshop_db`;

CREATE TABLE IF NOT EXISTS `password_resets` (
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

ALTER TABLE `products` ADD COLUMN `tags` VARCHAR(500) DEFAULT NULL AFTER `brand`;
ALTER TABLE `admin` ADD COLUMN `reset_token` VARCHAR(255) DEFAULT NULL AFTER `avatar_url`;
ALTER TABLE `admin` ADD COLUMN `reset_expiry` DATETIME DEFAULT NULL AFTER `reset_token`;
