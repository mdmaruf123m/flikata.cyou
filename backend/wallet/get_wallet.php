<?php
/**
 * Get User Wallet Balance, Transactions & Active Deposit Methods
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$userId = !empty($_GET['user_id']) ? (int)$_GET['user_id'] : ($auth ? (int)$auth['id'] : 0);

if (!$userId) {
    jsonResponse(false, null, 'User ID or Authorization required.');
}

// 1. Fetch user wallet balance & info
$uStmt = $pdo->prepare("SELECT id, full_name, email, phone, profile_pic, wallet_balance FROM users WHERE id = ?");
$uStmt->execute([$userId]);
$user = $uStmt->fetch();

if (!$user) {
    jsonResponse(false, null, 'User not found.');
}

// 2. Fetch active deposit methods ONLY (is_active = 1)
$mStmt = $pdo->query("SELECT * FROM deposit_methods WHERE is_active = 1 OR is_active = '1' ORDER BY id ASC");
$rawMethods = $mStmt->fetchAll();
$methods = [];
$isHttps = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ||
           (isset($_SERVER['HTTP_X_FORWARDED_PROTO']) && $_SERVER['HTTP_X_FORWARDED_PROTO'] === 'https') ||
           (isset($_SERVER['SERVER_PORT']) && $_SERVER['SERVER_PORT'] == 443);
$host = $_SERVER['HTTP_HOST'] ?? 'flikata.cyou';
$baseHostUrl = ($isHttps ? "https://" : "http://") . $host;

foreach ($rawMethods as $m) {
    if ((int)$m['is_active'] !== 1) continue;
    $img = $m['image_url'] ?? '';
    if (!empty($img) && !preg_match('#^https?://#i', $img)) {
        $img = rtrim($baseHostUrl, '/') . '/' . ltrim($img, '/');
    }
    $methods[] = [
        'id'          => (int)$m['id'],
        'name'        => (string)$m['name'],
        'number'      => (string)$m['number'],
        'image_url'   => $img ?: null,
        'description' => (string)($m['description'] ?? ''),
        'is_active'   => 1
    ];
}

// 2b. Fetch active withdraw methods ONLY (is_active = 1)
$wmStmt = $pdo->query("SELECT * FROM withdraw_methods WHERE is_active = 1 OR is_active = '1' ORDER BY id ASC");
$rawWithdrawMethods = $wmStmt->fetchAll();
$withdrawMethods = [];
foreach ($rawWithdrawMethods as $wm) {
    if ((int)$wm['is_active'] !== 1) continue;
    $img = $wm['image_url'] ?? '';
    if (!empty($img) && !preg_match('#^https?://#i', $img)) {
        $img = rtrim($baseHostUrl, '/') . '/' . ltrim($img, '/');
    }
    $withdrawMethods[] = [
        'id'          => (int)$wm['id'],
        'name'        => (string)$wm['name'],
        'number'      => (string)$wm['number'],
        'image_url'   => $img ?: null,
        'description' => (string)($wm['description'] ?? ''),
        'min_amount'  => (float)($wm['min_amount'] ?? 50.00),
        'max_amount'  => (float)($wm['max_amount'] ?? 25000.00),
        'is_active'   => 1
    ];
}

// 3. Fetch user wallet transactions
$tStmt = $pdo->prepare("SELECT * FROM wallet_transactions WHERE user_id = ? ORDER BY id DESC LIMIT 50");
$tStmt->execute([$userId]);
$transactions = $tStmt->fetchAll();

// 4. Fetch user recent deposit requests
$dStmt = $pdo->prepare("SELECT * FROM deposit_requests WHERE user_id = ? ORDER BY id DESC LIMIT 20");
$dStmt->execute([$userId]);
$depositRequests = $dStmt->fetchAll();

// 5. Fetch user recent withdraw requests
$wStmt = $pdo->prepare("SELECT * FROM withdraw_requests WHERE user_id = ? ORDER BY id DESC LIMIT 20");
$wStmt->execute([$userId]);
$withdrawRequests = $wStmt->fetchAll();

jsonResponse(true, [
    'wallet_balance'    => (float)($user['wallet_balance'] ?? 0.00),
    'account_id'        => (string)$user['id'],
    'user_name'         => $user['full_name'],
    'methods'           => $methods,
    'withdraw_methods'  => $withdrawMethods,
    'transactions'      => $transactions,
    'deposit_requests'  => $depositRequests,
    'withdraw_requests' => $withdrawRequests
], 'Wallet loaded.');
