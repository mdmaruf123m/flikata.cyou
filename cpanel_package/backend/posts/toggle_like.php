<?php
/**
 * Toggle Post Like Endpoint
 * Flikata API - Synchronized with post_reactions
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
$postId = (int)($input['post_id'] ?? 0);

if (!$postId) {
    jsonResponse(false, null, 'Invalid post ID.');
}

$stmt = $pdo->prepare("SELECT id FROM post_likes WHERE post_id = ? AND user_id = ?");
$stmt->execute([$postId, $user['id']]);
$existing = $stmt->fetch();

if ($existing) {
    // Unlike
    $del = $pdo->prepare("DELETE FROM post_likes WHERE post_id = ? AND user_id = ?");
    $del->execute([$postId, $user['id']]);

    $delReac = $pdo->prepare("DELETE FROM post_reactions WHERE post_id = ? AND user_id = ?");
    $delReac->execute([$postId, $user['id']]);

    $upd = $pdo->prepare("UPDATE posts SET likes_count = GREATEST(0, likes_count - 1) WHERE id = ?");
    $upd->execute([$postId]);

    $cnt = $pdo->prepare("SELECT likes_count FROM posts WHERE id = ?");
    $cnt->execute([$postId]);
    $likesCount = (int)$cnt->fetchColumn();

    jsonResponse(true, ['is_liked' => false, 'likes_count' => $likesCount, 'emoji' => null], 'Unliked post.');
} else {
    // Like
    $ins = $pdo->prepare("INSERT INTO post_likes (post_id, user_id) VALUES (?, ?)");
    $ins->execute([$postId, $user['id']]);

    $insReac = $pdo->prepare("
        INSERT INTO post_reactions (post_id, user_id, emoji) 
        VALUES (?, ?, 'Like') 
        ON DUPLICATE KEY UPDATE emoji = 'Like'
    ");
    $insReac->execute([$postId, $user['id']]);

    $upd = $pdo->prepare("UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?");
    $upd->execute([$postId]);

    $cnt = $pdo->prepare("SELECT likes_count FROM posts WHERE id = ?");
    $cnt->execute([$postId]);
    $likesCount = (int)$cnt->fetchColumn();

    jsonResponse(true, ['is_liked' => true, 'likes_count' => $likesCount, 'emoji' => 'Like'], 'Liked post.');
}
