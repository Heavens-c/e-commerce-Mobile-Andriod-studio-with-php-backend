<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

header('Content-Type: application/json; charset=UTF-8');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['status' => 'error', 'message' => 'Method not allowed'], JSON_UNESCAPED_UNICODE);
    exit();
}

$pdo      = getDBConnection();
$creds    = getLoginCredentials();
$rawEmail = trim((string) ($creds['email'] ?? ''));
$password = (string) ($creds['password'] ?? '');
$email    = sanitize($rawEmail);

if ($email === '' || $password === '') {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Missing email or password'], JSON_UNESCAPED_UNICODE);
    exit();
}

// ── Regular user ───────────────────────────────────────────────────────────
$stmt = $pdo->prepare('SELECT * FROM users WHERE email = ? AND is_active = 1');
$stmt->execute([$email]);
$user = $stmt->fetch(PDO::FETCH_ASSOC);

if ($user && password_verify($password, $user['password'])) {
    $token  = generateToken((int) $user['id']);
    $expiry = date('Y-m-d H:i:s', strtotime('+' . TOKEN_EXPIRY_HOURS . ' hours'));

    $pdo->prepare('UPDATE users SET auth_token = ?, token_expiry = ?, last_login = NOW() WHERE id = ?')
        ->execute([$token, $expiry, $user['id']]);

    $payloadUser = [
        'id'         => (int) $user['id'],
        'full_name'  => $user['full_name'],
        'email'      => $user['email'],
        'phone'      => $user['phone'],
        'avatar_url' => $user['avatar_url'],
        'address'    => $user['address'],
        'city'       => $user['city'],
        'province'   => $user['province'],
        'zip_code'   => $user['zip_code'],
        'country'    => $user['country'] ?? null,
        'membership' => $user['membership'],
        'coins'      => (int) $user['coins'],
        'points'     => (int) $user['points'],
    ];

    http_response_code(200);
    echo json_encode([
        'status'  => 'success',
        'message' => 'Login successful',
        'token'   => $token,
        'user'    => $payloadUser,
        'admin'   => null,
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// ── Admin ───────────────────────────────────────────────────────────────────
$stmt = $pdo->prepare('SELECT * FROM admin WHERE email = ? AND is_active = 1');
$stmt->execute([$email]);
$admin = $stmt->fetch(PDO::FETCH_ASSOC);

if ($admin && password_verify($password, $admin['password'])) {
    $token  = generateToken((int) $admin['id']);
    $expiry = date('Y-m-d H:i:s', strtotime('+' . TOKEN_EXPIRY_HOURS . ' hours'));

    $pdo->prepare('UPDATE admin SET auth_token = ?, token_expiry = ?, last_login = NOW() WHERE id = ?')
        ->execute([$token, $expiry, $admin['id']]);

    http_response_code(200);
    echo json_encode([
        'status'  => 'success',
        'message' => 'Login successful',
        'token'   => $token,
        'admin'   => [
            'id'   => (int) $admin['id'],
            'name' => $admin['full_name'],
        ],
        'user'    => null,
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

http_response_code(401);
echo json_encode([
    'status'  => 'error',
    'message' => 'Invalid credentials',
], JSON_UNESCAPED_UNICODE);
exit();
