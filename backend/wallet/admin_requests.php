<?php
/**
 * Admin API: Deposit Requests Management
 * Flikata Admin Panel
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$action = !empty($_REQUEST['action']) ? trim($_REQUEST['action']) : '';
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

if ($action === 'approve') {
    $requestId = !empty($input['request_id']) ? (int)$input['request_id'] : 0;
    if (!$requestId) {
        jsonResponse(false, null, 'Request ID required.');
    }

    $rStmt = $pdo->prepare("SELECT * FROM deposit_requests WHERE id = ?");
    $rStmt->execute([$requestId]);
    $req = $rStmt->fetch();

    if (!$req) {
        jsonResponse(false, null, 'Deposit request not found.');
    }

    if ($req['status'] === 'approved') {
        jsonResponse(false, null, 'Request is already approved.');
    }

    $userId = (int)$req['user_id'];
    $amount = (float)$req['amount'];

    $pdo->beginTransaction();
    try {
        // Credit user wallet
        $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?")->execute([$amount, $userId]);

        // Record transaction
        $desc = 'Deposit via ' . $req['method_name'] . ' (Trx: ' . $req['transaction_id'] . ')';
        $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'deposit', ?, ?, 'completed')")
            ->execute([$userId, $amount, $desc]);

        // Update request status
        $pdo->prepare("UPDATE deposit_requests SET status = 'approved', updated_at = NOW() WHERE id = ?")->execute([$requestId]);

        $pdo->commit();
        jsonResponse(true, ['amount' => $amount], 'Deposit approved successfully! Funds credited to user wallet.');
    } catch (Exception $e) {
        $pdo->rollBack();
        jsonResponse(false, null, 'Approval failed: ' . $e->getMessage());
    }
}

if ($action === 'reject') {
    $requestId = !empty($input['request_id']) ? (int)$input['request_id'] : 0;
    $reason = !empty($input['reason']) ? trim($input['reason']) : 'Invalid transaction ID or payment not received.';

    if (!$requestId) {
        jsonResponse(false, null, 'Request ID required.');
    }

    $stmt = $pdo->prepare("UPDATE deposit_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
    $stmt->execute([$reason, $requestId]);

    jsonResponse(true, null, 'Deposit request rejected.');
}

// Default: List requests by status tab (pending, approved, rejected, all)
$tab = !empty($_GET['status']) ? strtolower(trim($_GET['status'])) : 'pending';

$sql = "
    SELECT 
        dr.*,
        u.full_name AS user_name,
        u.profile_pic AS user_avatar,
        u.email AS user_email,
        u.phone AS user_phone,
        u.wallet_balance AS current_balance
    FROM deposit_requests dr
    LEFT JOIN users u ON dr.user_id = u.id
";

if (in_array($tab, ['pending', 'approved', 'rejected'])) {
    $sql .= " WHERE dr.status = " . $pdo->quote($tab);
}

$sql .= " ORDER BY dr.id DESC LIMIT 100";

$stmt = $pdo->query($sql);
$requests = $stmt->fetchAll();

$counts = [
    'pending'  => (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'pending'")->fetchColumn(),
    'approved' => (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'approved'")->fetchColumn(),
    'rejected' => (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'rejected'")->fetchColumn(),
];

jsonResponse(true, [
    'counts'   => $counts,
    'requests' => $requests
], 'Deposit requests loaded.');
