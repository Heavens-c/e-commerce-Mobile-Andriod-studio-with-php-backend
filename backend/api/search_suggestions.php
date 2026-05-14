<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();

$q = isset($_GET['q']) ? trim((string) $_GET['q']) : '';
$limit = isset($_GET['limit']) ? min(15, max(1, (int) $_GET['limit'])) : 8;

if (strlen($q) < 2) {
    sendResponse(200, true, 'Suggestions', ['suggestions' => []]);
}

$like = '%' . sanitizeLike($q) . '%';

$sql = <<<SQL
SELECT DISTINCT p.id, p.name, p.image_url, c.name AS category_name
FROM products p
LEFT JOIN categories c ON c.id = p.category_id
WHERE p.is_active = 1
  AND (
    p.name LIKE ? OR p.brand LIKE ? OR p.description LIKE ?
    OR c.name LIKE ?
    OR (p.tags IS NOT NULL AND p.tags LIKE ?)
    OR (p.variations IS NOT NULL AND CAST(p.variations AS CHAR) LIKE ?)
  )
ORDER BY (p.name LIKE ?) DESC, p.sold_count DESC
LIMIT $limit
SQL;

$stmt = $pdo->prepare($sql);
$stmt->execute([$like, $like, $like, $like, $like, $like, $like]);
$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

foreach ($rows as &$r) {
    $r['id'] = (int) $r['id'];
}
sendResponse(200, true, 'Suggestions', ['suggestions' => $rows]);

/** LIKE wildcard safety: strip % and _ from user input for use inside %...% */
function sanitizeLike(string $value): string {
    return str_replace(['%', '_'], ['', ''], $value);
}
