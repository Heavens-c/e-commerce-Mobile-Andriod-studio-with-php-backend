<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$data = getJsonInput();
validateRequired($data, ['full_name', 'email', 'password']);

$fullName = sanitize($data['full_name']);
$email = sanitize($data['email']);
$password = $data['password'];
$phone = isset($data['phone']) ? sanitize($data['phone']) : null;

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendResponse(400, false, 'Invalid email format');
}
if (strlen($password) < 6) {
    sendResponse(400, false, 'Password must be at least 6 characters');
}

$stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
$stmt->execute([$email]);
if ($stmt->fetch()) {
    sendResponse(409, false, 'Email already registered');
}

$hashedPassword = password_hash($password, PASSWORD_DEFAULT);
$verificationCode = str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT);
$token = generateToken(0);
$expiry = date('Y-m-d H:i:s', strtotime('+' . TOKEN_EXPIRY_HOURS . ' hours'));

$stmt = $pdo->prepare("INSERT INTO users (full_name, email, password, phone, verification_code, auth_token, token_expiry) VALUES (?, ?, ?, ?, ?, ?, ?)");
$stmt->execute([$fullName, $email, $hashedPassword, $phone, $verificationCode, $token, $expiry]);

$userId = $pdo->lastInsertId();

// Update token with real user id
$token = generateToken($userId);
$stmt = $pdo->prepare("UPDATE users SET auth_token = ? WHERE id = ?");
$stmt->execute([$token, $userId]);

sendResponse(201, true, 'Registration successful', [
    'token' => $token,
    'user' => [
        'id' => (int)$userId,
        'full_name' => $fullName,
        'email' => $email,
        'phone' => $phone,
        'membership' => 'standard',
        'coins' => 0,
        'points' => 0
    ]
]);
