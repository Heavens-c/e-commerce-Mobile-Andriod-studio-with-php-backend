<?php
// .htaccess for the backend API directory
// Place in the /api/ folder

header('Content-Type: application/json');

// Deny directory listing
// Options -Indexes

// Note: Create an actual .htaccess file with these rules:
// RewriteEngine On
// RewriteCond %{REQUEST_FILENAME} !-f
// RewriteRule ^(.*)$ index.php [QSA,L]
//
// <FilesMatch "\.(php|html)$">
//     Header set Access-Control-Allow-Origin "*"
// </FilesMatch>

echo json_encode([
    'success' => true,
    'message' => 'FlashShop API v1.0',
    'endpoints' => [
        'POST /api/login.php',
        'POST /api/register.php',
        'POST /api/forgot_password.php',
        'POST /api/logout.php',
        'GET  /api/get_products.php',
        'GET  /api/get_categories.php',
        'GET  /api/get_banners.php',
        'POST /api/add_to_cart.php',
        'GET  /api/get_cart.php',
        'POST /api/update_cart.php',
        'POST /api/wishlist.php',
        'GET  /api/wishlist.php',
        'POST /api/place_order.php',
        'GET  /api/get_orders.php',
        'GET  /api/reviews.php?product_id=X',
        'POST /api/reviews.php?product_id=X',
        'GET  /api/profile.php',
        'PUT  /api/profile.php',
        'GET  /api/get_notifications.php'
    ]
]);
