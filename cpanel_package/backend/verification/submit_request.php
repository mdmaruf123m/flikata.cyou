<?php
/**
 * Submit Verification Request & Deduct from Wallet
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$userId = $auth ? (int)$auth['id'] : (!empty($input['user_id']) ? (int)$input['user_id'] : 0);
$packageId = !empty($input['package_id']) ? (int)$input['package_id'] : 0;
$idFrontUrl = !empty($input['id_front_url']) ? trim($input['id_front_url']) : null;
$idBackUrl = !empty($input['id_back_url']) ? trim($input['id_back_url']) : null;
$selfieUrl = !empty($input['selfie_url']) ? trim($input['selfie_url']) : null;

if (!$userId || !$packageId) {
    jsonResponse(false, null, 'User ID and Package selection required.');
}

// 1. Fetch package
$pStmt = $pdo->prepare("SELECT * FROM verification_packages WHERE id = ? AND is_active = 1");
$pStmt->execute([$packageId]);
$package = $pStmt->fetch();

if (!$package) {
    jsonResponse(false, null, 'Selected verification package not found.');
}

$price = (float)$package['price'];

// 2. Fetch settings
$sStmt = $pdo->query("SELECT * FROM verification_settings WHERE id = 1");
$settings = $sStmt->fetch() ?: ['id_card_required' => 0, 'selfie_required' => 0];

if (!empty($settings['id_card_required'])) {
    if (empty($idFrontUrl) || empty($idBackUrl)) {
        jsonResponse(false, null, 'ID Card front and back images are required.');
    }
}

if (!empty($settings['selfie_required'])) {
    if (empty($selfieUrl)) {
        jsonResponse(false, null, 'Selfie photo is required.');
    }
}

// 3. Check user wallet balance
$uStmt = $pdo->prepare("SELECT id, wallet_balance, full_name FROM users WHERE id = ? FOR UPDATE");
$pdo->beginTransaction();

try {
    $uStmt->execute([$userId]);
    $user = $uStmt->fetch();

    if (!$user) {
        $pdo->rollBack();
        jsonResponse(false, null, 'User not found.');
    }

    $walletBalance = (float)($user['wallet_balance'] ?? 0.00);

    if ($walletBalance < $price) {
        $pdo->rollBack();
        jsonResponse(false, [
            'insufficient_balance' => true,
            'wallet_balance'       => $walletBalance,
            'required_price'       => $price
        ], 'Insufficient wallet balance. Please deposit funds first.');
    }

    // Deduct from wallet
    $newBalance = $walletBalance - $price;
    $upUser = $pdo->prepare("UPDATE users SET wallet_balance = ? WHERE id = ?");
    $upUser->execute([$newBalance, $userId]);

    // Record wallet transaction
    $tx = $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'verification_fee', ?, ?, 'completed')");
    $tx->execute([$userId, $price, 'Verification Badge: ' . $package['title']]);

    // Insert verification request
    $ins = $pdo->prepare("INSERT INTO verification_requests 
        (user_id, package_id, package_title, amount, id_front_url, id_back_url, selfie_url, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')");
    $ins->execute([
        $userId,
        $packageId,
        $package['title'],
        $price,
        $idFrontUrl,
        $idBackUrl,
        $selfieUrl
    ]);
    $requestId = (int)$pdo->lastInsertId();

    $pdo->commit();

    jsonResponse(true, [
        'request_id'     => $requestId,
        'new_balance'    => $newBalance,
        'package_title'  => $package['title'],
        'amount_deducted'=> $price
    ], 'Verification request submitted successfully! Awaiting admin review.');
} catch (Exception $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    jsonResponse(false, null, 'Failed to submit verification request: ' . $e->getMessage());
}
