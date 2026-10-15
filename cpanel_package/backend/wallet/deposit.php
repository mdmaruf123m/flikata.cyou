<?php
/**
 * Submit Deposit Request
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$userId = $auth ? (int)$auth['id'] : (!empty($input['user_id']) ? (int)$input['user_id'] : 0);
$methodId = !empty($input['method_id']) ? (int)$input['method_id'] : 0;
$methodName = !empty($input['method_name']) ? trim($input['method_name']) : '';
$amount = !empty($input['amount']) ? (float)$input['amount'] : 0.00;
$trxId = !empty($input['transaction_id']) ? trim($input['transaction_id']) : '';

if (!$userId || $amount <= 0 || empty($trxId)) {
    jsonResponse(false, null, 'User ID, valid deposit amount, and Transaction ID are required.');
}

// Fetch method name if missing
if (empty($methodName) && $methodId) {
    $mStmt = $pdo->prepare("SELECT name FROM deposit_methods WHERE id = ?");
    $mStmt->execute([$methodId]);
    $methodName = $mStmt->fetchColumn() ?: 'Online Payment';
}

$stmt = $pdo->prepare("INSERT INTO deposit_requests (user_id, method_id, method_name, amount, transaction_id, status) VALUES (?, ?, ?, ?, ?, 'pending')");
$stmt->execute([$userId, $methodId, $methodName, $amount, $trxId]);
$reqId = (int)$pdo->lastInsertId();

jsonResponse(true, [
    'deposit_id'     => $reqId,
    'amount'         => $amount,
    'transaction_id' => $trxId,
    'status'         => 'pending'
], 'Deposit request submitted successfully! Your funds will be credited once verified by admin.');
