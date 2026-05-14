<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo   = getDBConnection();
authenticateAdmin($pdo);

$sql = <<<SQL
SELECT o.id,
       COALESCE(NULLIF(TRIM(u.full_name), ''), u.email, 'Guest') AS user_name,
       o.total AS total_price,
       o.order_status AS raw_status,
       o.created_at
FROM orders o
JOIN users u ON u.id = o.user_id
ORDER BY o.created_at DESC
SQL;

$stmt   = $pdo->query($sql);
$orders = [];

while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $orders[] = [
        'id'           => (int) $row['id'],
        'user_name'    => $row['user_name'],
        'total_price'  => (float) $row['total_price'],
        'status'       => dbOrderStatusToAdminUi($row['raw_status']),
        'created_at'   => $row['created_at'],
    ];
}

sendResponse(200, true, 'Orders retrieved', ['orders' => $orders]);
