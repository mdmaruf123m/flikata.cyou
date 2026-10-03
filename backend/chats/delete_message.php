<?php
/**
 * Delete Message Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$messageId = (int)($input['message_id'] ?? 0);

if (!$messageId) {
    jsonResponse(false, null, 'Message ID is required.');
}

$stmt = $pdo->prepare("SELECT id FROM messages WHERE id = ? AND (sender_id = ? OR receiver_id = ?)");
$stmt->execute([$messageId, $user['id'], $user['id']]);
if (!$stmt->fetch()) {
    jsonResponse(false, null, 'You do not have permission to delete this message.');
}

$del = $pdo->prepare("DELETE FROM messages WHERE id = ?");
$del->execute([$messageId]);

jsonResponse(true, null, 'Message deleted.');
