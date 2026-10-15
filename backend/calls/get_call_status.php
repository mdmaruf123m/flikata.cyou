<?php
/**
 * Get Call Status (Polled by caller and receiver during ringing/connecting)
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$callId = !empty($_REQUEST['call_id']) ? (int)$_REQUEST['call_id'] : 0;
if (!$callId) {
    jsonResponse(false, null, 'Call ID required.');
}

$stmt = $pdo->prepare("SELECT * FROM calls WHERE id = ? LIMIT 1");
$stmt->execute([$callId]);
$call = $stmt->fetch();

if (!$call) {
    jsonResponse(false, null, 'Call not found.');
}

// Auto-expire ringing calls after 45 seconds
if ($call['status'] === 'ringing' && (time() - strtotime($call['created_at'])) > 45) {
    $pdo->prepare("UPDATE calls SET status = 'missed' WHERE id = ?")->execute([$callId]);
    $call['status'] = 'missed';
}

jsonResponse(true, [
    'call_id'          => (int)$call['id'],
    'status'           => $call['status'],
    'channel_name'     => $call['channel_name'],
    'duration_seconds' => (int)$call['duration_seconds']
], 'Call status retrieved.');
