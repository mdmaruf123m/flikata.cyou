<?php
/**
 * Mark Notifications as Read
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$notifId = !empty($input['id']) ? (int)$input['id'] : 0;

if ($notifId) {
    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?");
    $stmt->execute([$notifId, $user['id']]);
} else {
    // Mark all as read
    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE user_id = ?");
    $stmt->execute([$user['id']]);
}

jsonResponse(true, null, 'Notifications marked as read.');
