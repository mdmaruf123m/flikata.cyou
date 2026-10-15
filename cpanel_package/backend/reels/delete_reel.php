<?php
/**
 * Delete Reel Endpoint
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

$reelId = (int)($input['reel_id'] ?? 0);
if (!$reelId) {
    jsonResponse(false, null, 'Invalid reel ID.');
}

// Check if reel exists
$stmt = $pdo->prepare("SELECT * FROM reels WHERE id = ?");
$stmt->execute([$reelId]);
$reel = $stmt->fetch();

if (!$reel) {
    jsonResponse(true, null, 'Reel already deleted.');
}

// Clean up associated child records
$pdo->prepare("DELETE FROM reel_likes WHERE reel_id = ?")->execute([$reelId]);
$pdo->prepare("DELETE FROM comments WHERE reel_id = ?")->execute([$reelId]);

// If there's an associated post in the posts feed with this media_url, delete it too
if (!empty($reel['media_url'])) {
    $pdo->prepare("DELETE FROM posts WHERE video_url = ?")->execute([$reel['media_url']]);
}

$del = $pdo->prepare("DELETE FROM reels WHERE id = ?");
$del->execute([$reelId]);

jsonResponse(true, null, 'Reel deleted permanently.');
