<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(405, false, 'Method not allowed');
}

$pdo = getDBConnection();
authenticateAdmin($pdo);

if (empty($_FILES['image']) || !is_uploaded_file($_FILES['image']['tmp_name'] ?? '')) {
    sendResponse(400, false, 'Missing image file (field name: image)');
}

$result = uploadFile($_FILES['image'], 'products');
if (isset($result['error'])) {
    sendResponse(400, false, $result['error']);
}

sendResponse(200, true, 'Uploaded', ['url' => $result['url']]);
