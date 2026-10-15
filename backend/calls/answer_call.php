<?php
/**
 * Answer / Accept Audio Call
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$callId = !empty($input['call_id']) ? (int)$input['call_id'] : 0;

if (!$callId) {
    jsonResponse(false, null, 'Call ID required.');
}

$stmt = $pdo->prepare("UPDATE calls SET status = 'connected', updated_at = NOW() WHERE id = ? AND status = 'ringing'");
$stmt->execute([$callId]);

jsonResponse(true, ['status' => 'connected'], 'Call answered.');
