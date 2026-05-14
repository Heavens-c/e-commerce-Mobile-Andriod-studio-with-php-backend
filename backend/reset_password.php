<?php
/**
 * Browser password reset form (HTML).
 * Deploy under same host as your API; set reset_link_base in smtp_local.php.
 */
define('FLASHSHOP_SKIP_HTTP_HEADERS', true);
require_once __DIR__ . '/config/db.php';

$pdo = getDBConnection();
$token = isset($_GET['token']) ? preg_replace('/[^a-f0-9]/i', '', $_GET['token']) : '';
$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = isset($_POST['token']) ? preg_replace('/[^a-f0-9]/i', '', (string) $_POST['token']) : '';
    $pass  = isset($_POST['password']) ? (string) $_POST['password'] : '';
    $pass2 = isset($_POST['password_confirm']) ? (string) $_POST['password_confirm'] : '';

    if (strlen($token) < 32) {
        $error = 'Invalid reset link.';
    } elseif (strlen($pass) < 6) {
        $error = 'Password must be at least 6 characters.';
    } elseif ($pass !== $pass2) {
        $error = 'Passwords do not match.';
    } else {
        $stmt = $pdo->prepare('SELECT * FROM password_resets WHERE token = ? AND expires_at > NOW() LIMIT 1');
        $stmt->execute([$token]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        if (!$row) {
            $error = 'This reset link is invalid or has expired.';
        } else {
            $hash = password_hash($pass, PASSWORD_DEFAULT);
            if ($row['account_type'] === 'admin') {
                $pdo->prepare('UPDATE admin SET password = ?, reset_token = NULL, reset_expiry = NULL WHERE id = ?')
                    ->execute([$hash, $row['user_id']]);
            } else {
                $pdo->prepare('UPDATE users SET password = ?, reset_token = NULL, reset_expiry = NULL WHERE id = ?')
                    ->execute([$hash, $row['user_id']]);
            }
            $pdo->prepare('DELETE FROM password_resets WHERE email = ?')->execute([$row['email']]);
            $success = 'Your password has been updated. You can close this page and sign in.';
        }
    }
}

$tokenValid = false;
if ($error === '' && $success === '' && strlen($token) >= 32) {
    $stmt = $pdo->prepare('SELECT id FROM password_resets WHERE token = ? AND expires_at > NOW() LIMIT 1');
    $stmt->execute([$token]);
    $tokenValid = (bool) $stmt->fetch();
    if (!$tokenValid) {
        $error = 'This reset link is invalid or has expired.';
    }
}

header('Content-Type: text/html; charset=UTF-8');
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FlashShop — Reset password</title>
    <style>
        * { box-sizing: border-box; }
        body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; background: #f0f2f8; margin: 0; padding: 24px; }
        .card { max-width: 420px; margin: 40px auto; background: #fff; border-radius: 16px; padding: 28px; box-shadow: 0 8px 32px rgba(79,55,138,.12); }
        h1 { color: #4f378a; font-size: 1.35rem; margin: 0 0 8px; }
        p { color: #494551; font-size: 14px; line-height: 1.5; }
        label { display: block; font-size: 13px; font-weight: 600; color: #1d1b20; margin-top: 14px; }
        input[type=password] { width: 100%; padding: 12px 14px; margin-top: 6px; border: 1px solid #cbc4d2; border-radius: 10px; font-size: 15px; }
        button { width: 100%; margin-top: 22px; padding: 14px; border: 0; border-radius: 12px; background: #6750a4; color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
        button:hover { background: #5a4490; }
        .err { background: #ffdad6; color: #93000a; padding: 12px 14px; border-radius: 10px; font-size: 14px; margin-bottom: 12px; }
        .ok { background: #d4edda; color: #155724; padding: 12px 14px; border-radius: 10px; font-size: 14px; margin-bottom: 12px; }
    </style>
</head>
<body>
<div class="card">
    <h1>Reset your password</h1>
    <?php if ($success): ?>
        <div class="ok"><?php echo htmlspecialchars($success, ENT_QUOTES, 'UTF-8'); ?></div>
    <?php elseif ($error): ?>
        <div class="err"><?php echo htmlspecialchars($error, ENT_QUOTES, 'UTF-8'); ?></div>
        <p><a href="javascript:history.back()">Go back</a></p>
    <?php elseif ($tokenValid): ?>
        <p>Choose a new password for your FlashShop account.</p>
        <form method="post" action="">
            <input type="hidden" name="token" value="<?php echo htmlspecialchars($token, ENT_QUOTES, 'UTF-8'); ?>">
            <label for="password">New password</label>
            <input type="password" id="password" name="password" required minlength="6" autocomplete="new-password">
            <label for="password_confirm">Confirm password</label>
            <input type="password" id="password_confirm" name="password_confirm" required minlength="6" autocomplete="new-password">
            <button type="submit">Update password</button>
        </form>
    <?php else: ?>
        <p>Missing reset token. Open the link from your email.</p>
    <?php endif; ?>
</div>
</body>
</html>
