<?php
/**
 * Toggle Reel Like Endpoint
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

$reelId   = (int)($input['reel_id'] ?? 0);
$reaction = trim($input['reaction'] ?? 'Like');

if (!$reelId) {
    jsonResponse(false, null, 'Invalid reel ID.');
}

$stmt = $pdo->prepare("SELECT id FROM reel_likes WHERE reel_id = ? AND user_id = ?");
$stmt->execute([$reelId, $user['id']]);
$existing = $stmt->fetch();

if ($existing) {
    // Unlike
    $del = $pdo->prepare("DELETE FROM reel_likes WHERE reel_id = ? AND user_id = ?");
    $del->execute([$reelId, $user['id']]);

    $upd = $pdo->prepare("UPDATE reels SET likes_count = GREATEST(0, likes_count - 1) WHERE id = ?");
    $upd->execute([$reelId]);

    $cntStmt = $pdo->prepare("SELECT likes_count FROM reels WHERE id = ?");
    $cntStmt->execute([$reelId]);
    $newCount = (int)$cntStmt->fetchColumn();

    jsonResponse(true, ['is_liked' => false, 'likes_count' => $newCount, 'reaction' => null], 'Unliked reel.');
} else {
    // Like
    $ins = $pdo->prepare("INSERT INTO reel_likes (reel_id, user_id, reaction) VALUES (?, ?, ?)");
    $ins->execute([$reelId, $user['id'], $reaction]);

    $upd = $pdo->prepare("UPDATE reels SET likes_count = likes_count + 1 WHERE id = ?");
    $upd->execute([$reelId]);

    $cntStmt = $pdo->prepare("SELECT likes_count FROM reels WHERE id = ?");
    $cntStmt->execute([$reelId]);
    $newCount = (int)$cntStmt->fetchColumn();

    jsonResponse(true, ['is_liked' => true, 'likes_count' => $newCount, 'reaction' => $reaction], 'Liked reel.');
}
