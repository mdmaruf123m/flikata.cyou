<?php
/**
 * End / Reject Audio Call
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$callId = !empty($input['call_id']) ? (int)$input['call_id'] : (!empty($_REQUEST['call_id']) ? (int)$_REQUEST['call_id'] : 0);
$reason = !empty($input['reason']) ? trim($input['reason']) : (!empty($_REQUEST['reason']) ? trim($_REQUEST['reason']) : 'ended');
$duration = !empty($input['duration']) ? (int)$input['duration'] : 0;

if (!$callId) {
    jsonResponse(false, null, 'Call ID required.');
}

$stmt = $pdo->prepare("UPDATE calls SET status = ?, duration_seconds = ?, updated_at = NOW() WHERE id = ?");
$stmt->execute([$reason, $duration, $callId]);

// Also insert a call record notice in conversation if conversation exists
$cStmt = $pdo->prepare("SELECT * FROM calls WHERE id = ?");
$cStmt->execute([$callId]);
$c = $cStmt->fetch();
if ($c) {
    $durStr = $duration > 0 ? sprintf('%02d:%02d', floor($duration / 60), $duration % 60) : '';
    $noticeText = ($reason === 'declined') ? 'Declined audio call' : ($duration > 0 ? '📞 Audio call ended • ' . $durStr : 'Missed audio call');

    $convStmt = $pdo->prepare("
        SELECT id FROM conversations 
        WHERE (user_one_id = ? AND user_two_id = ?) 
           OR (user_one_id = ? AND user_two_id = ?) 
        LIMIT 1
    ");
    $convStmt->execute([$c['caller_id'], $c['receiver_id'], $c['receiver_id'], $c['caller_id']]);
    $convId = $convStmt->fetchColumn();

    if ($convId) {
        $msgIns = $pdo->prepare("
            INSERT INTO messages (conversation_id, sender_id, receiver_id, text, is_call_notice, is_read, created_at)
            VALUES (?, ?, ?, ?, 1, 1, NOW())
        ");
        $msgIns->execute([$convId, $c['caller_id'], $c['receiver_id'], $noticeText]);
        $pdo->prepare("UPDATE conversations SET last_message = ?, updated_at = NOW() WHERE id = ?")->execute([$noticeText, $convId]);
    }
}

jsonResponse(true, ['status' => $reason], 'Call ended.');
