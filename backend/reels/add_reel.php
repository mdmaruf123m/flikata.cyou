<?php
/**
 * Add New Reel Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$user = getAuthUser($pdo);
if (!$user && !empty($input['user_id'])) {
    $uStmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
    $uStmt->execute([$input['user_id']]);
    $user = $uStmt->fetch() ?: null;
}

if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$mediaUrl   = trim($input['media_url'] ?? '');
$caption    = trim($input['caption'] ?? '');
$audioTitle = trim($input['audio_title'] ?? 'Original Audio - ' . $user['full_name']);
$bucketId   = !empty($input['bucket_id']) ? (int)$input['bucket_id'] : null;

if (!$mediaUrl) {
    jsonResponse(false, null, 'Video media URL is required.');
}

$stmt = $pdo->prepare("
    INSERT INTO reels (user_id, media_url, caption, audio_title, bucket_id)
    VALUES (?, ?, ?, ?, ?)
");
$stmt->execute([$user['id'], $mediaUrl, $caption, $audioTitle, $bucketId]);
$reelId = $pdo->lastInsertId();

jsonResponse(true, ['reel_id' => $reelId], 'Reel uploaded successfully!');
