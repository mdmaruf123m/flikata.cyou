<?php
/**
 * Check Incoming Call (Polled by receiver)
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$userId = $auth ? (int)$auth['id'] : (!empty($_REQUEST['user_id']) ? (int)$_REQUEST['user_id'] : 0);

if (!$userId) {
    jsonResponse(false, null, 'User ID required.');
}

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

// Mark current user as online with fresh timestamp
$pdo->prepare("UPDATE users SET is_online = 1, updated_at = NOW() WHERE id = ?")->execute([$userId]);

// Check for active ringing call created in the last 40 seconds
$stmt = $pdo->prepare("
    SELECT c.*, u.full_name AS caller_name, u.profile_pic AS caller_avatar, u.is_verified AS caller_verified
    FROM calls c
    JOIN users u ON c.caller_id = u.id
    WHERE c.receiver_id = ? 
      AND c.status = 'ringing'
      AND c.created_at >= (NOW() - INTERVAL 40 SECOND)
    ORDER BY c.id DESC 
    LIMIT 1
");
$stmt->execute([$userId]);
$call = $stmt->fetch();

if ($call) {
    jsonResponse(true, [
        'has_call'        => true,
        'call_id'         => (int)$call['id'],
        'channel_name'    => $call['channel_name'],
        'caller_id'       => (int)$call['caller_id'],
        'receiver_id'     => (int)$call['receiver_id'],
        'caller_name'     => $call['caller_name'],
        'caller_avatar'   => $call['caller_avatar'] ?: '',
        'caller_verified' => (bool)$call['caller_verified'],
        'status'          => $call['status']
    ], 'Incoming call found.');
} else {
    jsonResponse(true, [
        'has_call' => false
    ], 'No incoming call.');
}
