<?php
/**
 * Admin API: Verification Requests Management
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

    $rStmt = $pdo->prepare("SELECT vr.*, vp.duration_type, vp.duration_value FROM verification_requests vr LEFT JOIN verification_packages vp ON vr.package_id = vp.id WHERE vr.id = ?");
    $rStmt->execute([$requestId]);
    $req = $rStmt->fetch();

    if (!$req) {
        jsonResponse(false, null, 'Verification request not found.');
    }

    $userId = (int)$req['user_id'];
    $durType = !empty($req['duration_type']) ? $req['duration_type'] : 'month';
    $durVal = max(1, (int)($req['duration_value'] ?? 1));

    $verifiedUntil = null;
    if ($durType === 'lifetime') {
        $verifiedUntil = '2099-12-31 23:59:59';
    } elseif ($durType === 'day') {
        $verifiedUntil = date('Y-m-d H:i:s', strtotime("+$durVal days"));
    } elseif ($durType === 'month') {
        $verifiedUntil = date('Y-m-d H:i:s', strtotime("+$durVal months"));
    } elseif ($durType === 'year') {
        $verifiedUntil = date('Y-m-d H:i:s', strtotime("+$durVal years"));
    } else {
        $verifiedUntil = date('Y-m-d H:i:s', strtotime("+1 month"));
    }

    $pdo->beginTransaction();
    try {
        $uUp = $pdo->prepare("UPDATE users SET is_verified = 1, verified_until = ? WHERE id = ?");
        $uUp->execute([$verifiedUntil, $userId]);

        $rUp = $pdo->prepare("UPDATE verification_requests SET status = 'approved', updated_at = NOW() WHERE id = ?");
        $rUp->execute([$requestId]);

        $pdo->commit();
        jsonResponse(true, ['verified_until' => $verifiedUntil], 'Verification request approved successfully! User is now verified.');
    } catch (Exception $e) {
        $pdo->rollBack();
        jsonResponse(false, null, 'Approval failed: ' . $e->getMessage());
    }
}

if ($action === 'reject') {
    $requestId = !empty($input['request_id']) ? (int)$input['request_id'] : 0;
    $reason = !empty($input['reason']) ? trim($input['reason']) : 'Documentation verification requirements not met.';

    if (!$requestId) {
        jsonResponse(false, null, 'Request ID required.');
    }

    $rStmt = $pdo->prepare("SELECT * FROM verification_requests WHERE id = ?");
    $rStmt->execute([$requestId]);
    $req = $rStmt->fetch();

    if (!$req) {
        jsonResponse(false, null, 'Verification request not found.');
    }

    $userId = (int)$req['user_id'];
    $refundAmount = (float)$req['amount'];

    $pdo->beginTransaction();
    try {
        $rUp = $pdo->prepare("UPDATE verification_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
        $rUp->execute([$reason, $requestId]);

        // Refund wallet balance if amount was deducted
        if ($refundAmount > 0) {
            $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?")->execute([$refundAmount, $userId]);
            $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'refund', ?, ?, 'completed')")
                ->execute([$userId, $refundAmount, 'Refund: Verification rejected (' . $reason . ')']);
        }

        $pdo->commit();
        jsonResponse(true, null, 'Verification request rejected and fee refunded to user wallet.');
    } catch (Exception $e) {
        $pdo->rollBack();
        jsonResponse(false, null, 'Rejection failed: ' . $e->getMessage());
    }
}

// Default: List requests by tab status (pending, approved, rejected, all)
$tab = !empty($_GET['status']) ? strtolower(trim($_GET['status'])) : 'pending';

$sql = "
    SELECT 
        vr.*,
        u.full_name AS user_name,
        u.profile_pic AS user_avatar,
        u.email AS user_email,
        u.phone AS user_phone,
        vp.duration_type,
        vp.duration_value
    FROM verification_requests vr
    LEFT JOIN users u ON vr.user_id = u.id
    LEFT JOIN verification_packages vp ON vr.package_id = vp.id
";

if (in_array($tab, ['pending', 'approved', 'rejected'])) {
    $sql .= " WHERE vr.status = " . $pdo->quote($tab);
}

$sql .= " ORDER BY vr.id DESC LIMIT 100";

$stmt = $pdo->query($sql);
$requests = $stmt->fetchAll();

// Counts for tabs
$counts = [
    'pending'  => (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'pending'")->fetchColumn(),
    'approved' => (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'approved'")->fetchColumn(),
    'rejected' => (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'rejected'")->fetchColumn(),
];

jsonResponse(true, [
    'counts'   => $counts,
    'requests' => $requests
], 'Requests loaded.');
