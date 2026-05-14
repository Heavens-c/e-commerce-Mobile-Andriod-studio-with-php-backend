<?php
/**
 * Copy this file to smtp_local.php in the same folder and fill in real values.
 * Never commit smtp_local.php with production passwords.
 *
 * reset_link_base = browser URL to the folder containing reset_password.php (no trailing slash).
 * Example: http://localhost/flashshop/backend
 */
return [
    'smtp_host'       => 'smtp.gmail.com',
    'smtp_port'       => 587,
    'smtp_user'       => '',
    'smtp_pass'       => '',
    'smtp_from'       => '',
    'smtp_from_name'  => 'FlashShop',
    'reset_link_base' => 'http://localhost/flashshop/backend',
];
