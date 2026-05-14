<?php
/**
 * FlashShop — Forgot password (JSON API)
 * Stores token in password_resets, sends HTML email via PHPMailer + SMTP.
 *
 * Setup:
 *  1. Run database/migrations/001_flashshop_extensions.sql
 *  2. composer require phpmailer/phpmailer  (in backend/)
 *  3. Copy backend/config/smtp.example.php → smtp_local.php and fill SMTP + reset_link_base
 */
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$composerAutoload = __DIR__ . '/../../vendor/autoload.php';
$manualAutoload   = __DIR__ . '/../libs/PHPMailer/src/PHPMailer.php';

if (file_exists($composerAutoload)) {
    require_once $composerAutoload;
} elseif (file_exists($manualAutoload)) {
    require_once __DIR__ . '/../libs/PHPMailer/src/Exception.php';
    require_once __DIR__ . '/../libs/PHPMailer/src/PHPMailer.php';
    require_once __DIR__ . '/../libs/PHPMailer/src/SMTP.php';
} else {
    sendResponse(500, false, 'Email service not configured. Install PHPMailer (composer require phpmailer/phpmailer).');
}

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

$smtpFile = __DIR__ . '/../config/smtp_local.php';
if (!file_exists($smtpFile)) {
    sendResponse(500, false, 'Copy config/smtp.example.php to config/smtp_local.php and configure SMTP.');
}
$smtp = require $smtpFile;

$pdo  = getDBConnection();
$data = getJsonInput();
validateRequired($data, ['email']);

$email = sanitize(trim((string) $data['email']));
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendResponse(400, false, 'Invalid email format');
}

$user = null;
$accountType = null;

$stmt = $pdo->prepare('SELECT id, full_name FROM users WHERE email = ? AND is_active = 1');
$stmt->execute([$email]);
$row = $stmt->fetch(PDO::FETCH_ASSOC);
if ($row) {
    $user = $row;
    $accountType = 'user';
}

if (!$user) {
    $stmt = $pdo->prepare('SELECT id, full_name FROM admin WHERE email = ? AND is_active = 1');
    $stmt->execute([$email]);
    $row = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($row) {
        $user = $row;
        $accountType = 'admin';
    }
}

if (!$user) {
    sendResponse(200, true, 'If that email is registered, a reset link has been sent.');
}

$token       = bin2hex(random_bytes(32));
$expiresAt   = date('Y-m-d H:i:s', strtotime('+1 hour'));
$resetBase   = rtrim((string) ($smtp['reset_link_base'] ?? ''), '/');
if ($resetBase === '') {
    $resetBase = rtrim(defined('RESET_LINK_BASE') ? RESET_LINK_BASE : BASE_URL, '/');
}
$resetLink = $resetBase . '/reset_password.php?token=' . urlencode($token);

$pdo->prepare('DELETE FROM password_resets WHERE email = ?')->execute([$email]);
$ins = $pdo->prepare(
    'INSERT INTO password_resets (email, token, account_type, user_id, expires_at) VALUES (?,?,?,?,?)'
);
$ins->execute([$email, $token, $accountType, (int) $user['id'], $expiresAt]);

$mail = new PHPMailer(true);
try {
    $mail->isSMTP();
    $mail->Host       = $smtp['smtp_host'] ?? 'smtp.gmail.com';
    $mail->SMTPAuth   = true;
    $mail->Username   = $smtp['smtp_user'] ?? '';
    $mail->Password   = $smtp['smtp_pass'] ?? '';
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
    $mail->Port       = (int) ($smtp['smtp_port'] ?? 587);

    $from     = $smtp['smtp_from'] ?? $smtp['smtp_user'];
    $fromName = $smtp['smtp_from_name'] ?? 'FlashShop';
    $mail->setFrom($from, $fromName);
    $mail->addAddress($email, $user['full_name']);

    $mail->isHTML(true);
    $mail->Subject = 'FlashShop — Password reset';
    $mail->Body    = buildResetEmailHtml((string) $user['full_name'], $resetLink);
    $mail->AltBody = "Reset your password (valid 1 hour):\n\n$resetLink\n";

    $mail->send();
    sendResponse(200, true, 'If that email is registered, a reset link has been sent.');
} catch (Exception $e) {
    error_log('FlashShop forgot_password SMTP: ' . $mail->ErrorInfo);
    sendResponse(500, false, 'Could not send reset email. Check SMTP settings in config/smtp_local.php.');
}

function buildResetEmailHtml(string $name, string $link): string {
    $safeName = htmlspecialchars($name, ENT_QUOTES, 'UTF-8');
    $safeLink = htmlspecialchars($link, ENT_QUOTES, 'UTF-8');
    return <<<HTML
<!DOCTYPE html>
<html><head><meta charset="UTF-8"></head>
<body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px;">
  <div style="max-width:500px;margin:auto;background:#fff;border-radius:12px;padding:32px;">
    <h2 style="color:#6750A4;margin-top:0;">FlashShop</h2>
    <p>Hi <strong>{$safeName}</strong>,</p>
    <p>We received a request to reset your password. The button below is valid for <strong>1 hour</strong>.</p>
    <div style="text-align:center;margin:28px 0;">
      <a href="{$safeLink}" style="background:#6750A4;color:#fff;padding:14px 28px;border-radius:10px;text-decoration:none;font-weight:bold;">Reset password</a>
    </div>
    <p style="color:#666;font-size:13px;">Or paste this link into your browser:<br><a href="{$safeLink}" style="color:#6750A4;word-break:break-all;">{$safeLink}</a></p>
    <p style="color:#999;font-size:12px;">If you did not request this, you can ignore this email.</p>
  </div>
</body></html>
HTML;
}
