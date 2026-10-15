<?php
/**
 * Mark Notifications as Read
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
if (!$user) {
    $uid = !empty($_REQUEST['user_id']) ? (int)$_REQUEST['user_id'] : 0;
    if ($uid > 0) {
        $uStmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
        $uStmt->execute([$uid]);
        $user = $uStmt->fetch();
    }
}

if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$notifId = !empty($input['id']) ? (int)$input['id'] : (!empty($_REQUEST['id']) ? (int)$_REQUEST['id'] : 0);

if ($notifId > 0) {
    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE id = ? AND (user_id = ? OR user_id = 0)");
    $stmt->execute([$notifId, (int)$user['id']]);
} else {
    // Mark all as read
    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE user_id = ? OR user_id = 0");
    $stmt->execute([(int)$user['id']]);
}

jsonResponse(true, null, 'Notifications marked as read.');
