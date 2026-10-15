<?php
/**
 * Initiate Audio Call
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

// Ensure calls table exists
$pdo->exec("CREATE TABLE IF NOT EXISTS `calls` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `caller_id` INT NOT NULL,
  `receiver_id` INT NOT NULL,
  `channel_name` VARCHAR(100) NOT NULL,
  `status` ENUM('ringing', 'connected', 'declined', 'ended', 'missed') DEFAULT 'ringing',
  `duration_seconds` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`caller_id`),
  INDEX (`receiver_id`),
  INDEX (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

$auth = getAuthUser($pdo);
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$callerId = $auth ? (int)$auth['id'] : (!empty($input['caller_id']) ? (int)$input['caller_id'] : 0);
$receiverId = !empty($input['receiver_id']) ? (int)$input['receiver_id'] : 0;

if (!$callerId || !$receiverId) {
    jsonResponse(false, null, 'Caller and receiver IDs are required.');
}

// Check receiver online status
$pdo->exec("UPDATE users SET is_online = 0 WHERE updated_at < (NOW() - INTERVAL 60 SECOND)");
$rStmt = $pdo->prepare("SELECT id, full_name, is_online, updated_at FROM users WHERE id = ?");
$rStmt->execute([$receiverId]);
$receiver = $rStmt->fetch();

if (!$receiver) {
    jsonResponse(false, null, 'User not found.');
}

$updatedTime = !empty($receiver['updated_at']) ? strtotime($receiver['updated_at']) : 0;
$secondsAgo = time() - $updatedTime;
$isReceiverOnline = ((int)$receiver['is_online'] === 1 && $secondsAgo <= 60);

if (!$isReceiverOnline) {
    jsonResponse(false, null, 'Offline');
}

// Mark any older ringing or active calls for these users as ended
$stmt = $pdo->prepare("UPDATE calls SET status = 'ended' WHERE (caller_id = ? OR receiver_id = ?) AND status IN ('ringing', 'connected')");
$stmt->execute([$callerId, $callerId]);

$channel = 'call_' . $callerId . '_' . $receiverId . '_' . time();

$ins = $pdo->prepare("INSERT INTO calls (caller_id, receiver_id, channel_name, status) VALUES (?, ?, ?, 'ringing')");
$ins->execute([$callerId, $receiverId, $channel]);
$callId = (int)$pdo->lastInsertId();

// Fetch caller info
$uStmt = $pdo->prepare("SELECT id, full_name, profile_pic, is_verified FROM users WHERE id = ?");
$uStmt->execute([$callerId]);
$caller = $uStmt->fetch();

jsonResponse(true, [
    'call_id'         => $callId,
    'channel_name'    => $channel,
    'caller_id'       => $callerId,
    'receiver_id'     => $receiverId,
    'caller_name'     => $caller ? $caller['full_name'] : 'User',
    'caller_avatar'   => $caller ? ($caller['profile_pic'] ?: '') : '',
    'caller_verified' => $caller ? (bool)$caller['is_verified'] : false,
    'status'          => 'ringing'
], 'Call initiated.');
