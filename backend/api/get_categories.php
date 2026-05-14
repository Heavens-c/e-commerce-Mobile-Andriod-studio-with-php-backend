<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();

$stmt = $pdo->prepare("SELECT * FROM categories WHERE is_active = 1 ORDER BY sort_order ASC");
$stmt->execute();
$categories = $stmt->fetchAll();

foreach ($categories as &$cat) {
    $cat['id'] = (int)$cat['id'];
    $cat['sort_order'] = (int)$cat['sort_order'];
    $cat['is_active'] = (bool)$cat['is_active'];
}

sendResponse(200, true, 'Categories retrieved', ['categories' => $categories]);
