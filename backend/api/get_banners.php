<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();

$stmt = $pdo->prepare("SELECT * FROM banners WHERE is_active = 1 AND (starts_at IS NULL OR starts_at <= NOW()) AND (ends_at IS NULL OR ends_at >= NOW()) ORDER BY sort_order ASC");
$stmt->execute();
$banners = $stmt->fetchAll();

foreach ($banners as &$b) {
    $b['id'] = (int)$b['id'];
}

sendResponse(200, true, 'Banners retrieved', ['banners' => $banners]);
