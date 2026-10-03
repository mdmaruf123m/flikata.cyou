<?php
/**
 * Edit Sent Message Endpoint
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
$newText   = trim($input['text'] ?? '');

if (!$messageId || !$newText) {
    jsonResponse(false, null, 'Message ID and updated text are required.');
}

$stmt = $pdo->prepare("SELECT id FROM messages WHERE id = ? AND sender_id = ?");
$stmt->execute([$messageId, $user['id']]);
if (!$stmt->fetch()) {
    jsonResponse(false, null, 'You can only edit your own messages.');
}

$upd = $pdo->prepare("UPDATE messages SET text = ?, is_edited = 1 WHERE id = ?");
$upd->execute([$newText, $messageId]);

jsonResponse(true, ['message_id' => $messageId, 'text' => $newText], 'Message updated.');
